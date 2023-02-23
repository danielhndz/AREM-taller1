# :hammer_and_wrench: Facade app for OMDbAPI

## Arquitecturas Empresariales

### :pushpin: Daniel Felipe Hernández Mancipe

<br/>

El proyecto es una app (servidor fachada) para consultar la información de películas de cine a través de su título. Información adicional:

- Se utiliza la API gratuita de [OMDbAPI](https://www.omdbapi.com/).

- Se implementa un [**Caché**](/src/main/java/edu/escuelaing/arep/cache/Cache.java) para evitar consultas repetidas a la API externa.

- El [servidor](/src/main/java/edu/escuelaing/arep/server/HttpServer.java) usa un pool de hilos para atender las peticiones de manera **concurrente**.

- Se utiliza un [cliente](/src/main/resources/main.js) **asíncrono** en el browser.

- Se utiliza un [cliente Java](/src/main/java/edu/escuelaing/arep/client/Client.java) para probar las funciones del [servidor fachada](/src/main/java/edu/escuelaing/arep/server/HttpServer.java). El cliente utiliza simples conexiones HTTP y WebSocket para conectarse a los servicios. Este cliente se utiliza para hacer [unit testing](/src/test/java/edu/escuelaing/arep/CacheAndConcurrencyTest.java) de concurrencia en el servidor.

![diseño enunciado](../media/enunciado.png?raw=true)

## Getting Started

### Prerequisites

- Java >= 11.x
- Maven >= 3.x
- Git >= 2.x
- JUnit 4.x

### Installing

Simplemente clone el repositorio:

```bash
git clone https://github.com/danielhndz/AREM-taller1.git
```

Luego compile el proyecto con maven:

```bash
cd <project-folder>
mvn compile
```

Si salió bien, debería tener una salida similar a esta:

![compile output](../media/mvn_compile.png?raw=true)

### Using

Para ejecutar correctamente debe estar en la carpeta raíz del proyecto. El servidor inicia por defecto con un pool de máximo 8 hilos, este valor también se puede pasar como argumento, por ejemplo 20:

```bash
mvn exec:java -Dexec.mainClass="edu.escuelaing.arep.Launcher"
```

![output for first use](../media/using1.png?raw=true)

```bash
mvn exec:java -Dexec.mainClass="edu.escuelaing.arep.Launcher" -Dexec.args="20"
```

![output for second use](../media/using2.png?raw=true)

Ahora puede conectarse al servidor desplegado en [localhost](https://localhost:35000/):

![connect from browser](../media/using3.png?raw=true)

Y buscar cualquier título deseado:

![search movie](../media/using4.png?raw=true)

Dado que la aplicación es **multiusuario**, si se usa una instancia en modo incógnito del browser, se puede usar el server independientemente de la primera sesión:

![multiuser example](../media/using5.png?raw=true)

Se puede bajar el servidor con una simple petición HTTP a [/exit](https://localhost:35000/exit):

![shutdown](../media/using6.png?raw=true)

En el apartado de Unit testing se demuestra el funcionamiento de la memoria caché y la concurrencia.

## Running the tests

La clase [CacheAndConcurrencyTest](/src/test/java/edu/escuelaing/arep/CacheAndConcurrencyTest.java) se compone principalmente de dos tests unitarios, ambos test buscan probar, por un lado la funcionalidad de la memoria Caché, y por el otro, la capacidad del servidor de atender peticiones concurrentes.

### Break down into end to end tests

- **concurrentTest1with1**: Prueba el funcionamiento cuando el servidor tiene un pool de máximo 1 hilo y atiende 1 cliente. En este test solo se busca 1 película, por lo que el tamaño de la memoria Caché debe terminar en 1, y en total, la [API externa](/src/main/java/edu/escuelaing/arep/apis/OMDbAPI.java) solo realiza 1 consulta.

```bash
mvn -Dtest=CacheAndConcurrencyTest#concurrentTest1with1 test
```

![test1](../media/test1.png?raw=true)

- **concurrentTest1with100**: Prueba el funcionamiento cuando el servidor tiene un pool de máximo 1 hilo y atiende 100 clientes de manera concurrente. En este test se buscan 4 películas diferentes, por lo que el tamaño de la memoria Caché debe terminar en 4, y en total, la [API externa](/src/main/java/edu/escuelaing/arep/apis/OMDbAPI.java) solo realiza 4 consultas para atender a los 100 clientes.

```bash
mvn -Dtest=CacheAndConcurrencyTest#concurrentTest1with100 test
```

![test2](../media/test2.png?raw=true)

**Para correr todos los tests unitarios a la vez**:

```bash
mvn test
```

![test3](../media/test3.png?raw=true)

## Built With

- [Maven](https://maven.apache.org/) - Dependency Management
- [Git](https://git-scm.com/) - Version Management
- [JUnit4](https://junit.org/junit4/) - Unit testing framework for Java

## Design Metaphor

- Autor: Daniel Hernández
- Última modificación: 02/02/2023

### Backend Class Diagram

- [Diagrama de paquetes](/src/main/java/edu/escuelaing/arep/)

![Diagrama de paquetes](../media/pkgs.png?raw=true)

Los nombres de los paquetes intentan ser representativos en términos de la funcionalidad que está implementada en dicho paquete. La clase [Launcher](/src/main/java/edu/escuelaing/arep/Launcher.java) arranca el proyecto.

![Diagrama de paquetes con clases](../media/pkgs_simple.png?raw=true)

- Dado que la persistencia de datos se encuentra implementada en memoria, es la clase [Cache](/src/main/java/edu/escuelaing/arep/cache/Cache.java) del paquete [Cache](/src/main/java/edu/escuelaing/arep/cache/) la que se encarga de almacenar las consultas que son creadas por los usuarios. Esta clase implementa el patrón `singleton`.

- La clase [HttpServer](/src/main/java/edu/escuelaing/arep/server/HttpServer.java) modelo el servidor mediante un [ServerSocket](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/ServerSocket.html) y un pool de hilos mediante un [ExecutorService](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ExecutorService.html). Esta clase implementa el patrón `Singleton`.

- La clase [OMDbAPI](/src/main/java/edu/escuelaing/arep/apis/OMDbAPI.java) del paquete [apis](/src/main/java/edu/escuelaing/arep/apis/) es la que se encarga de realizar las peticiones al API externo. Esta clase implementa el patrón `Singleton`.

- La clase [MovileSearchService](/src/main/java/edu/escuelaing/arep/services/MovieSearchService.java) del paquete [serviceas](/src/main/java/edu/escuelaing/arep/services/) modela el servicio de peticiones al API externo.

- La clase [FilesReader](/src/main/java/edu/escuelaing/arep/utils/FilesReader.java) del paquete [utils](/src/main/java/edu/escuelaing/arep/utils/) es la que se encarga de leer y devolver los recursos solicitados.

- La clase [RequestProcessor](/src/main/java/edu/escuelaing/arep/utils/FilesReader.java) del paquete [utils](/src/main/java/edu/escuelaing/arep/utils/) es la que se encarga de analizar y procesar las diferentes peticiones que llegan al servidor.

- La clase [Client](/src/main/java/edu/escuelaing/arep/client/Client.java) del paquete [client](/src/main/java/edu/escuelaing/arep/client//) es la que se encarga de modelar un cliente que quiere conectarse al servidor mediante un [Socket](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/Socket.html).

- La clase [IDClients](/src/main/java/edu/escuelaing/arep/client/IDClients.java) del paquete [client](/src/main/java/edu/escuelaing/arep/client//) es la que se encarga de generar un ID único para indentificar cada cliente que quiere conectarse al servidor.

## Authors

- **Daniel Hernández** - _Initial work_ - [danielhndz](https://github.com/danielhndz)

## License

This project is licensed under the GPLv3 License - see the [LICENSE.md](LICENSE.md) file for details

## Javadoc

Para generar Javadocs independientes para el proyecto en la carpeta `/target/site/apidocs` ejecute:

```bash
mvn javadoc:javadoc
```
