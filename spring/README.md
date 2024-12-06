# Call Automation - Quick Start Sample

In this quickstart, we cover how you can use Call Automation SDK to make an outbound call to a phone number and use the newly announced integration with Azure AI services to play dynamic prompts to participants using Text-to-Speech and recognize user voice input through Speech-to-Text to drive business logic in your application. 

# Design

## Before running the sample for the first time

- Open the application.yml file in the resources folder to configure the following settings

    - `connectionstring`: Azure Communication Service resource's connection string.
    - `callerphonenumber`: Phone number associated with the Azure Communication Service resource.

      Format: "OutboundTarget(Phone Number)".

          For e.g. "+1425XXXAAAA"
    - `basecallbackuri`: Base url of the app. For local development use dev tunnel url.
      ```


### Run the application

- Navigate to the directory containing the pom.xml file and use the following mvn commands:
    - Compile the application: mvn compile
    - Build the package: mvn package
    - Execute the app: mvn exec:java
- Access the Swagger UI at http://localhost:8000/swagger-ui.html
    - Try the POST /outboundCall to run the Sample Application
    ```
    curl -X 'POST' http://localhost:8000/outboundcall -H "Content-Type: application/json" -d '{"targetphonenumber":"+49221"}'
    ```
