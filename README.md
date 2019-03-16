# OpenLP-Convert

This project is a small Spring Boot sample project.  It was originally designed to provide a simple web interface to allow oneself to convert copy-pasted notes from one program into OpenLP's Openlyircs format.  It may not be terribly useful, but might serve as an example of how to use some Spring Boot technologies.

## Getting Started

Simply pull down the project and run the gradle wrapper.
```
./gradlew
```

### Prerequisites

You'll need a copy of Java 8 installed on your host.

## Deployment

As this is a Spring Boot app, just assemble the jar file and run it with Java.

```
./gradlew assemble
cd build/libs
java -jar openlp-convert.jar
```

## Built With
[Gradle](https://gradle.org/) - Dependency management

## Authors

* Thomas Brian Holdren - *Initial work* - [CyanFocus](https://www.cyanfocus.com/wordpress/)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Pivotal software for the awesome Spring Boot Platform.
* OpenLP for making some great free presentation software.
* Openlyric for attempting to standardize a format for exchange of songs.
