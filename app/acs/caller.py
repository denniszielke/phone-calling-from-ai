
from logging import INFO
from azure.eventgrid import EventGridEvent, SystemEventNames
from azure.core.messaging import CloudEvent
from typing import List, Optional, Union, TYPE_CHECKING
from azure.communication.callautomation import (
    CallAutomationClient,
    CallConnectionClient,
    PhoneNumberIdentifier,
    RecognizeInputType,
    MicrosoftTeamsUserIdentifier,
    CallInvite,
    RecognitionChoice,
    DtmfTone,
    VoiceKind,
    FileSource,
    TextSource)
import json
import requests

class OutboundCall:
    target_number: str
    target_text: str
    source_number: str
    acs_connection_string: str
    acs_callback_path: str
    ai_endpoint: str

    def __init__(self, source_number:str, acs_connection_string: str, acs_callback_path: str, ai_endpoint_url: str):
        self.source_number = source_number
        self.acs_connection_string = acs_connection_string
        self.acs_callback_path = acs_callback_path
        self.ai_endpoint = ai_endpoint_url
    
    async def call(self, target_number: str, text: str):
        self.target_number = target_number
        self.target_text = text
        self.call_automation_client = CallAutomationClient.from_connection_string(self.acs_connection_string)
        self.target_participant = PhoneNumberIdentifier(self.target_number)
        self.source_caller = PhoneNumberIdentifier(self.source_number)
        call_connection_properties = self.call_automation_client.create_call(self.target_participant, 
                                                                    self.acs_callback_path,
                                                                    source_caller_id_number=self.source_caller,
                                                                    cognitive_services_endpoint=self.ai_endpoint)

    async def _outbound_call_handler(self, request):
        print("Outbound call handler")
        json = await request.json()

        for event_dict in json:
            # Parsing callback events
            event = CloudEvent.from_dict(event_dict)
            call_connection_id = event.data['callConnectionId']
            print("%s event received for call connection id: %s", event.type, call_connection_id)
            call_connection_client = self.call_automation_client.get_call_connection(call_connection_id)
            target_participant = PhoneNumberIdentifier(self.target_number)
            if event.type == "Microsoft.Communication.CallConnected":
                print("Call connected")
                print(call_connection_id)
                my_file = FileSource(url="https://github.com/denniszielke/phone-calling-from-ai/raw/refs/heads/main/app/acs/thankyou.wav")
                await call_connection_client.play_media_to_all(my_file)

            #https://github.com/MicrosoftDocs/azure-docs/blob/main/articles/communication-services/quickstarts/call-automation/includes/quickstart-make-an-outbound-call-using-callautomation-python.md
                # Provide SourceLocale and VoiceKind to select an appropriate voice. 
                print("Playing text to target participant")
                play_source = TextSource(
                    text=self.target_text, source_locale="en-US", voice_kind=VoiceKind.FEMALE
                )
                play_to = [target_participant]
                await call_connection_client.get_call_connection(call_connection_id).play_media(
                    play_source=play_source, play_to=play_to
                )

    def attach_to_app(self, app, path):
        app.router.add_post(path, self._outbound_call_handler)
