## Preparation

1.) Create Multi AI Service https://learn.microsoft.com/en-gb/azure/communication-services/concepts/call-automation/azure-communication-services-azure-cognitive-services-integration

2.) Create managed identity. Assing managed identity permissions on KeyVault and Container Registry

3.) Create secrets in the keyvault for the following values

name: acssourcenumber
value: the acs source number

name: acsconnectionstring
value: the acs connection string

name: aiserviceendpoint
value: the ai service endpoint

4.) deploy container apps

## Deployment scripts

```

SERVICE_NAME=caller-api
RESOURCE_GROUP=rg-dzagents


echo "building and deploying the api"
bash ./azd-hooks/deploy.sh $SERVICE_NAME $RESOURCE_GROUP

```