# Distributed Bookstore

[![Java](https://img.shields.io/badge/Java-21.x-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.9.11-BB2045?logo=apachemaven&logoColor=white)](https://maven.apache.org/ref/3.9.11/)
[![MariaDB](https://img.shields.io/badge/MariaDB-10.11.x-003545?logo=mariadb&logoColor=white)](https://mariadb.com/docs/release-notes/community-server/mariadb-10-11-series)
[![Apache Tomcat](https://img.shields.io/badge/Tomcat-10.1+-CCA111?logo=apachetomcat&logoColor=white)](https://tomcat.apache.org/tomcat-10.1-doc/index.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This application uses Java RMI and Jakarta XML Web Services to manage a distributed bookstore through Remote Method Invocations (RMI) and Remote Procedure Calls (RPC) respectively. There are three apps in this repository: the server, admin client and user client.

## Architecture

The server has a dual-service architecture for showcasing two distinct types of remote communication in distributed systems:

1. **Java RMI (Remote Method Invocation):** Enables direct Java-to-Java interaction through a tightly-coupled, high-performance communication layer. The client uses this method for inter-process communication.
2. **JAX-WS (SOAP/RPC):** Makes administrative functions available from a wider range of platforms if necessary by exposing a standardized, interoperable web service endpoint. The admin client uses this method to communicate with the server.

### User Client: Java RMI

The User Client connects to the server using Java RMI, allowing it to invoke methods on server-side objects as if they were local. The communication between the User Client and the Server follows a standard RMI layered architecture.

![RMI Layered Architecture](/assets/rmi.png)

- **Application Layer:** Represents the `User Client` and the `Server`'s service coordination logic.
- **Stubs:** Acts as a client's local representative or proxy for the remote object. In RMI, a stub for a remote object implements the same set of remote interfaces that a remote object implements.
- **Skeletons:** Responsible for dispatching the call to the actual remote object implementation.
- **Remote Reference Layer:** Determines how the remote object is invoked which could be determined based on replication, activation, reference behavior, etc.
- **Transport Layer:** Manages the low level network implementation.

### Admin Client: JAX-WS (SOAP/RPC)

JAX-WS is the protocol is used by the Admin Client. This is more standardized and interoperable than RMI. An administrative contract (WSDL) is created between the server and the client. A dynamic proxy is established by the Admin to facilitate communication between server's web service endpoint and SOAP/XML.

![JAX-WS Client Workflow](/assets/rpc.png)

1. **WSDL to Java:** The client uses the server's published WSDL (Web Services Description Language) to generate a `Service Endpoint Interface`.
2. **Get Proxy:** The client uses the generated service class to request a proxy instance that implements the `RPCServiceInterface`.
3. **Invoke Method:** When an admin function like `createBook(book)` is called on the proxy instance, the parameters (JAXB-generated class instances) are passed to it.
4. **SOAP Request/Response:** The proxy serializes the method call into a SOAP XML request and sends it to the server's **Endpoint URL**. The server processes the request and returns a SOAP response.
5. **Return Value:** The proxy deserializes the SOAP response back into a Java object and returns it to the client application.

## Features

| Area | User Client (RMI) | Admin Client (RPC) | Server |
| :--- | :--- | :--- | :--- |
| **Core Function** | Bookstore Browser | Inventory Management | Application & Data Engine |
| **Data Rights** | Read-Only | CRUD | System of Record |
| **Book Operations**| View, Search, Check Stock | Create, Update, Delete | Expose All Ops |
| **Bulk Data** | Not Supported | CSV Upload | Bulk Insert Endpoint |
| **Protocol** | RMI Consumer | SOAP/RPC Consumer | RMI & SOAP Provider |
| **DB Access** | None (via API) | None (via API) | Direct (JDBC) |

## GUIs

### User Client (RMI)

*For browsing and searching the book catalog.*

![User Client GUI](/assets/client.png)

### Admin Client (RPC)

*For managing the bookstore inventory (CRUD operations).*

![Admin Client GUI](/assets/admin.png)

## Project Setup

1. Clone the Repository

    ```bash
    git clone git git@github.com:shama-llama/distributed-bookstore.git
    cd distributed-bookstore
    ```

2. Build Modules

    ```bash
    # Build server
    mvn clean install -f bookstore-server/pom.xml
    
    # Build admin client
    mvn clean install -f bookstore-admin/pom.xml
    
    # Build user client
    mvn clean install -f bookstore-user/pom.xml
    ```

## Database Setup

1. Install MariaDB

    ```bash
    sudo dnf install mariadb-server
    ```

2. Start MariaDB service

    ```bash
    sudo systemctl start mariadb
    ```

3. Secure Installation

    ```bash
    sudo mysql_secure_installation
    ```

4. Create Bookstore

    ```sql
    mysql -u root -p
    
    CREATE DATABASE bookstore DEFAULT CHARACTER SET utf8mb4 COLLATE     utf8mb4_unicode_ci;
    ```

5. Create Books

    ```sql
    USE bookstore;

    CREATE TABLE books (
        isbn VARCHAR(13) PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        author VARCHAR(255) NOT NULL,
        year INT NOT NULL,
        price DECIMAL(10,2) NOT NULL,
        quantity INT NOT NULL DEFAULT 0
    );
    ```

6. Update DB Configuration

    Edit `bookstore-server/src/main/resources/db.properties`:

    ```properties
    db.url=jdbc:mysql://localhost:3306/bookstore
    db.user=root
    db.password=root_password
    ```

## Running the Application

1. Start the RMI Server

    ```bash
    cd bookstore-server
    mvn exec:java -Dexec.mainClass="rmi.RMIServer"
    ```

    > The RMI server will start on port 1099.

2. Start the RPC Server

    ```bash
    cd bookstore-server
    mvn exec:java -Dexec.mainClass="rpc.RPCServer"
    ```

    > The RPC server will start on port 8080.

3. Run the Admin Client

    ```bash
    cd bookstore-admin
    mvn exec:java -Dexec.mainClass="AdminClient"
    ```

4. Run the User Client

    ```bash
    cd bookstore-user
    mvn exec:java -Dexec.mainClass="UserClient"
    ```

## Troubleshooting

- MariaDB must be running for the apps to work: sudo systemctl status mariadb.
- Change the database credentials in db.properties.
- Create the bookstore database and books table before running.
- Check if ports 1099 and 8080 are being used.
- Clear Maven cache if you're getting build errors: mvn clean.
- RPC server must be running when building admin client.

## License

This project is licensed under the terms of the [MIT](LICENSE) open source license.
