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
    def tag(self, user_id, latitude, longitude):
        self.SaveToDB(user_id, latitude, longitude)

    def SaveToDB(self, user_id, latitude, longitude):
        db = pymysql.connect(self.db_location, self.db_user_name, self.db_pwd, self.db_database_name)
        cursor = db.cursor()
        cursor.execute("call spNewOption({0}, {1}, {2})".format(user_id, latitude, longitude))
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
