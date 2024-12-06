package com.communication.callautomation;

import com.azure.communication.callautomation.CallAutomationClient;
import com.azure.communication.callautomation.CallAutomationClientBuilder;
import com.azure.communication.callautomation.CallAutomationEventParser;
import com.azure.communication.callautomation.CallMedia;
import com.azure.communication.callautomation.models.*;
import com.azure.communication.callautomation.models.events.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ProgramSample {
    private AppConfig appConfig;
    private CallAutomationClient client;

    public ProgramSample(final AppConfig appConfig) {
        this.appConfig = appConfig;
        client = initClient();
    }

    @PostMapping(path = "/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok().body("Welcome to Call Automation API");
    }

    @PostMapping(path = "/outboundcall")
    public ResponseEntity<String> outboundCall(@RequestBody final Map<String, String> reqBody) {

        String targetPhoneNumber = reqBody.get("targetphonenumber");

        String callConnectionId = createOutboundCall(targetPhoneNumber);
        return ResponseEntity.ok().body("Target participant: "
                + targetPhoneNumber +
                ", CallConnectionId: " + callConnectionId);
    }

    @PostMapping(path = "/api/callback")
    public ResponseEntity<String> callbackEvents(@RequestBody final String reqBody) {
        List<CallAutomationEventBase> events = CallAutomationEventParser.parseEvents(reqBody);
        log.info("Received call events: {}", Arrays.toString(events.toArray()));
        for (CallAutomationEventBase event : events) {
            String callConnectionId = event.getCallConnectionId();
            log.info(
                    "Received call event callConnectionID: {}, serverCallId: {}",
                    callConnectionId,
                    event.getServerCallId());

            if (event instanceof CallConnected) {
                log.info("Call onnected on callConnectionId: {}", callConnectionId);
                FileSource fileSource = new FileSource().setUrl("https://" + appConfig.getBaseUri() + "/threattrigger.wav");
                CallMedia media = client.getCallConnection(callConnectionId).getCallMedia();
                for (int i = 0; i < 3; i++) {
                    media.playToAll(fileSource);
                    log.info("Playing audio file to all participants");
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        log.error("Interrupted exception: ", e);
                    }
                }
                hangUp(callConnectionId);
            }            
            else if(event instanceof PlayCompleted || event instanceof PlayFailed) {
                log.info("Received Play Completed event. Terminating call");
                hangUp(callConnectionId);
            }
        }
        return ResponseEntity.ok().body("");
    }

    private String createOutboundCall(String targetPhoneNumber) {
        try {
            PhoneNumberIdentifier caller = new PhoneNumberIdentifier(appConfig.getCallerPhoneNumber());
            PhoneNumberIdentifier target = new PhoneNumberIdentifier(targetPhoneNumber);
            CallInvite callInvite = new CallInvite(target, caller);
            CreateCallOptions createCallOptions = new CreateCallOptions(callInvite, "https://" + appConfig.getBaseUri() + "/api/callback");
            Response<CreateCallResult> result = client.createCallWithResponse(createCallOptions, Context.NONE);
            return result.getValue().getCallConnectionProperties().getCallConnectionId();
        } catch (CommunicationErrorResponseException e) {
            log.error("Error when creating call: {} {}",
                    e.getMessage(),
                    e.getCause());
            return "";
        }
    }
    
    private void hangUp(final String callConnectionId) {
        try {
            client.getCallConnection(callConnectionId).hangUp(true);
            log.info("Terminated call");
        } catch (Exception e) {
            log.error("Error when terminating the call for all participants {} {}",
                    e.getMessage(),
                    e.getCause());
        }
    }

    private CallAutomationClient initClient() {
        CallAutomationClient client;
        try {
            client = new CallAutomationClientBuilder()
                    .connectionString(appConfig.getConnectionString())
                    .buildClient();
            return client;
        } catch (NullPointerException e) {
            log.error("Please verify if Application config is properly set up");
            return null;
        } catch (Exception e) {
            log.error("Error occurred when initializing Call Automation Async Client: {} {}",
                    e.getMessage(),
                    e.getCause());
            return null;
        }
    }
}
