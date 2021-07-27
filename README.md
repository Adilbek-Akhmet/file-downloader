инструкция для UBUNTU
1) Проверте есть ли java: java --version и если есть и это java 11 или выше не нужно установить
2) Если нет перейдите https://www.oracle.com/java/technologies/javase-jdk16-downloads.html
3) скачайте Linux x64Debian Package
4) sudo dpkg -i jdk-16.0.2_linux-x64_bin.deb
5) sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-16.0.2/bin/java 1
6) sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-16.0.2/bin/javac 1
7) java --version