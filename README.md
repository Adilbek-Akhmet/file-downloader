
**application-production.properties**
1) нужно изменить значение 
   ~~~~
   spring.servlet.multipart.max-file-size
   spring.servlet.multipart.max-request-size
   
   folder.storage.path
   super.admin.username
   super.admin.password
   download.domain
   download.prefix
   
**systemd**
1) cd /etc/systemd/system
2) sudo touch fileDownload.service
3) sudo nano fileDownload.service
4) Выставте текст на fileDownload.service

~~~~
[Unit]
Description=File Download
After=syslog.target network.target

[Service]

User=root

Type=simple

WorkingDirectory=
ExecStart=/usr/bin/java -jar FileDownload-0.0.1-SNAPSHOT.jar --spring.profiles.active=production --spring.config.location=application-production.properties
Restart=always
RestartSec=60

[Install]
WantedBy=multi-user.target
~~~~

5) В этом файле fileDownload.service укажите WorkingDirectory
6) Сохраните файл
7) 
~~~~
sudo systemctl daemon-reload
sudo systemctl start fileDownload.service
sudo systemctl status fileDownload.service
