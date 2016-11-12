import cherrypy
import pymysql
import simplejson as json
from decimal import Decimal

class GetOptions(object):
    db_location = "fantel.czkthrrqljas.us-east-1.rds.amazonaws.com"
    db_database_name = "fantel"
    db_user_name = "fantel"
    db_pwd = ""

    @cherrypy.expose
    def index(self, latitude, longitude):
        all_options = self.getAllJSON(latitude, longitude)
        return all_options

    @cherrypy.expose
    def tag(self, latitude, longitude, userid, datetagged, dateuntagged):
        print("RECEIVED::latitude:::" + str(latitude) + "::longitude:" + str(longitude))
        bFound = False
        # two cases. User tagged a new option or 2) user untagged an option.
        all_options = self.getAllFromDB(False, latitude, longitude)

        for item in all_options:
            # the order the columns appear in the database
            item_id, item_global, item_latitude, item_longitude, item_userid, item_date_tagged, item_date_untagged, item_dist = item

            print("testing:" + ":latitude:" + str(item_latitude) + ":longitude:" + str(item_longitude))

            if Decimal(latitude) == item_latitude and \
                    Decimal(longitude) == item_longitude:
                bFound = True
                print("FOUND")
                # it's the same option
                if (dateuntagged != ""):
                    # they are not tagging it but untagging. use global_id.
                    # if it doesn't have a global_id, it was never sent to the cloud.
                    #  And it was untagged in the local db.
                    print("going to untag::: " + dateuntagged + ":::")
                    self.untag(item_global)
                    return
                break

        if (not bFound):
            self.SaveToDB(latitude, longitude, userid, datetagged, dateuntagged)

    def untag(self, global_id):
        try:
            db = pymysql.connect(self.db_location, self.db_user_name, self.db_pwd, self.db_database_name)
            cursor = db.cursor()
            exec_string = "call spUtagOption('%s')" % global_id
            print (exec_string)
            cursor.execute(exec_string)
            db.commit()
            db.close()
        except Exception as ex:
            print(ex)

    def SaveToDB(self, latitude, longitude, userid, datetagged, dateuntagged):
        try:
            db = pymysql.connect(self.db_location, self.db_user_name, self.db_pwd, self.db_database_name)
            cursor = db.cursor()
            exec_string = "call spNewOption(%s, %s, '%s', '%s', '%s')" % (
                latitude, longitude, userid, datetagged, dateuntagged)
            cursor.execute(exec_string)
            db.commit()
            db.close()
        except Exception as ex:
            print(ex)

    def getAllFromDB(self, drawing, latitude, longitude):
        nDrawingFlag = 1 if drawing else 0
        db = pymysql.connect(self.db_location, self.db_user_name, self.db_pwd, self.db_database_name)
        cursor = db.cursor()
        cursor.execute("call spDistance({0}, {1}, {2})".format(nDrawingFlag, latitude, longitude))
        data = cursor.fetchall()
        db.close()
        return data

    def getAllJSON(self, latitude, longitude):
        # True because we're only intersted in the drawables.
        data = self.getAllFromDB(True, latitude, longitude)

        final_json = []

        for item in data:
            # the order the columns appear in the database
            OptionsID, GlobalID, Latitude, Longitude, UserID, DateTagged, DateUntagged, distance = item

            DateTagged = str(DateTagged)
            DateUntagged = str(DateUntagged)

            dct = { "OptionsID" : OptionsID,
                    "GlobalID" : GlobalID,
                    "Latitude" : Latitude,
                    "Longitude" : Longitude,
                    "UserID" : UserID ,
                    "DateTagged" : DateTagged,
                    "DateUntagged" : DateUntagged,
                    "distance" : distance  }

            final_json.append(dct)

        return json.dumps(final_json)

if __name__ == '__main__':
    cherrypy.config.update({'log.screen': True,
                        'log.access_file': 'ax.log',
                        'log.error_file': 'err.log'})
    cherrypy.config.update({'server.socket_host': '0.0.0.0', 'server.socket_port': 8080, })
    cherrypy.quickstart(GetOptions())
