import cherrypy
import pymysql
import simplejson as json

class GetOptions(object):
    db_location = "fantel.czkthrrqljas.us-east-1.rds.amazonaws.com"
    db_database_name = "fantel"
    db_user_name = "fantel"
    db_pwd = ""

    @cherrypy.expose
    def index(self, latitude, longitude):
        all_options = self.getAllFromDB(latitude, longitude)
        return all_options

    @cherrypy.expose
    def tag(self, latitude, longitude, userid, datetagged, dateuntagged):
        self.SaveToDB(latitude, longitude, userid, datetagged, dateuntagged)

    def SaveToDB(self, latitude, longitude, userid, datetagged, dateuntagged):
        db = pymysql.connect(self.db_location, self.db_user_name, self.db_pwd, self.db_database_name)
        cursor = db.cursor()
        # exec_string = "call spNewOption({0}, {1}, '{2}', '{3}', '{4}')".format(latitude, longitude, userid, datetagged, dateuntagged)
        exec_string = "call spNewOption(%s, %s, '%s', '%s', '%s')" % (latitude, longitude, userid, datetagged, dateuntagged)
        cursor.execute(exec_string)
        db.close()

    def getAllFromDB(self, latitude, longitude):
        db = pymysql.connect(self.db_location, self.db_user_name, self.db_pwd, self.db_database_name)
        cursor = db.cursor()
        cursor.execute("call spDistance({0}, {1})".format(latitude, longitude))
        data = cursor.fetchall()

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

        db.close()

        return json.dumps(final_json)

if __name__ == '__main__':
    cherrypy.config.update({'server.socket_host': '0.0.0.0', 'server.socket_port': 8080, })
    cherrypy.quickstart(GetOptions())
