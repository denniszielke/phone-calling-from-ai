package com.communication.callautomation;

import com.azure.communication.callautomation.CallAutomationClient;
import com.azure.communication.callautomation.CallAutomationClientBuilder;
import com.azure.communication.callautomation.CallAutomationEventParser;
import com.azure.communication.callautomation.CallMedia;
import com.azure.communication.callautomation.models.*;
import com.azure.communication.callautomation.models.events.*;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class ProgramSample {
    private static final int MAX_PLAYS = 3;
    private static final String AUDIO_FILE_NAME = "threattrigger.wav";
    private static final String CALLBACK_PATH = "/api/callback";
    
    private final AppConfig appConfig;
    private final CallAutomationClient client;
    private final Map<String, Integer> audioPlayCount = new ConcurrentHashMap<>();

    public ProgramSample(final AppConfig appConfig) {
        this.appConfig = appConfig;
        this.client = initClient();
    }

    @PostMapping(path = "/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to Call Automation API");
    }

    @PostMapping(path = "/outboundcall")
    public ResponseEntity<String> outboundCall(@RequestBody final Map<String, String> reqBody) {
        log.info("Initiating outbound call");
        String targetPhoneNumber = reqBody.get("targetphonenumber");
        String callConnectionId = createOutboundCall(targetPhoneNumber);
        
        return ResponseEntity.ok(String.format("Target participant: %s, CallConnectionId: %s", 
                targetPhoneNumber, callConnectionId));
    }

    @PostMapping(path = CALLBACK_PATH)
    public ResponseEntity<String> callbackEvents(@RequestBody final String reqBody) {
        log.info("Processing callback events");
        List<CallAutomationEventBase> events = CallAutomationEventParser.parseEvents(reqBody);
        
        events.forEach(event -> {
            String callConnectionId = event.getCallConnectionId();
            log.info("Event: {} for callConnectionId: {}", event.getClass().getSimpleName(), callConnectionId);
            
            if (event instanceof CallConnected) {
                handleCallConnected(callConnectionId);
            } else if (event instanceof PlayCompleted) {
                handlePlayCompleted(callConnectionId);
            } else if (event instanceof PlayFailed) {
                log.info("Play failed event received. Terminating call");
                hangUp(callConnectionId);
            }
        });
        
        return ResponseEntity.ok("");
    }

    private void handleCallConnected(String callConnectionId) {
        log.info("Call connected on callConnectionId: {}", callConnectionId);
        audioPlayCount.put(callConnectionId, 1);
        playAudioFile(callConnectionId);
    }

    private void playAudioFile(String callConnectionId) {
        int currentPlay = audioPlayCount.getOrDefault(callConnectionId, 0);
        FileSource fileSource = new FileSource().setUrl("https://" + appConfig.getBaseUri() + "/" + AUDIO_FILE_NAME);
        
        client.getCallConnection(callConnectionId).getCallMedia()
              .playToAll(fileSource);
              
        log.info("Playing audio file to all participants (play #{}/{})", currentPlay, MAX_PLAYS);
    }

    private void handlePlayCompleted(String callConnectionId) {
        int playCount = audioPlayCount.getOrDefault(callConnectionId, 0);
        
        if (playCount < MAX_PLAYS) {
            audioPlayCount.put(callConnectionId, playCount + 1);
            playAudioFile(callConnectionId);
        } else {
            log.info("Completed all {} audio plays. Terminating call", MAX_PLAYS);
            hangUp(callConnectionId);
            audioPlayCount.remove(callConnectionId);
        }
    }

    private String createOutboundCall(String targetPhoneNumber) {
        try {
            PhoneNumberIdentifier caller = new PhoneNumberIdentifier(appConfig.getCallerPhoneNumber());
            PhoneNumberIdentifier target = new PhoneNumberIdentifier(targetPhoneNumber);
            
            CallInvite callInvite = new CallInvite(target, caller);
            String callbackUrl = "https://" + appConfig.getBaseUri() + CALLBACK_PATH;
            CreateCallOptions options = new CreateCallOptions(callInvite, callbackUrl);
            
            Response<CreateCallResult> result = client.createCallWithResponse(options, Context.NONE);
            String callConnectionId = result.getValue().getCallConnectionProperties().getCallConnectionId();
            log.info("Call created with callConnectionId: {}", callConnectionId);
            
            return callConnectionId;
        } catch (CommunicationErrorResponseException e) {
            log.error("Error when creating call: {}", e.getMessage(), e);
            return "";
        }
    }
    
    private void hangUp(final String callConnectionId) {
        try {
            client.getCallConnection(callConnectionId).hangUp(true);
            log.info("Terminated call for callConnectionId: {}", callConnectionId);
        } catch (Exception e) {
            log.error("Error when terminating the call: {}", e.getMessage(), e);
        }
    }

    private CallAutomationClient initClient() {
        try {
            return new CallAutomationClientBuilder()
                    .connectionString(appConfig.getConnectionString())
                    .buildClient();
        } catch (NullPointerException e) {
            log.error("Please verify if Application config is properly set up");
            return null;
        } catch (Exception e) {
            log.error("Error initializing Call Automation Client: {}", e.getMessage(), e);
            return null;
        }
    }
}
