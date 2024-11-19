
from logging import INFO
from azure.eventgrid import EventGridEvent, SystemEventNames
from azure.core.messaging import CloudEvent

from azure.communication.callautomation import (
    CallAutomationClient,
    CallConnectionClient,
    PhoneNumberIdentifier,
    RecognizeInputType,
    MicrosoftTeamsUserIdentifier,
    CallInvite,
    RecognitionChoice,
    DtmfTone,
    TextSource)


class OutboundCall:
    target_number: str
    source_number: str
    acs_connection_string: str
    acs_callback_path: str

    def __init__(self, target_number: str, source_number:str, acs_connection_string: str, acs_callback_path: str):
        self.target_number = target_number
        self.source_number = source_number
        self.acs_connection_string = acs_connection_string
        self.acs_callback_path = acs_callback_path
    
    async def call(self):
        call_automation_client = CallAutomationClient.from_connection_string(self.acs_connection_string)
        target_participant = PhoneNumberIdentifier(self.target_number)
        source_caller = PhoneNumberIdentifier(self.source_number)
        call_connection_properties = call_automation_client.create_call(target_participant, 
                                                                    self.acs_callback_path,
                                                                    source_caller_id_number=source_caller)

    def _outbound_call_handler():
        print("Outbound call handler")
        for event_dict in request.json:
            # Parsing callback events
            event = CloudEvent.from_dict(event_dict)
            call_connection_id = event.data['callConnectionId']
            print("%s event received for call connection id: %s", event.type, call_connection_id)
            call_connection_client = self.call_automation_client.get_call_connection(call_connection_id)
            target_participant = PhoneNumberIdentifier(self.target_number)
            if event.type == "Microsoft.Communication.CallConnected":
                print("Call connected")

    def attach_to_app(self, app, path):
        app.router.add_get(path, self._outbound_call_handler)
