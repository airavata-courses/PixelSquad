1. set up virtual for djang project
2. virtualenv venv
3. source venv/scripts/activate to activate the virtual environment
4. download dependencies 
5. migrate the database with (python manage.py makemigrations and migrate command)
6. for me, the path is like below
18126@DESKTOP-2ORIMG7 MINGW64 ~/Desktop/metadatasearch/metadataextractorsearch
$ ls
db.sqlite3  manage.py*  metadataextractorsearch/  searchapi/
(venv)

7. python manage.py runserver in order to run the server
