import logging
import os
from pathlib import Path
import requests
import json
from aiohttp import web
from azure.core.credentials import AzureKeyCredential
from azure.identity import AzureDeveloperCliCredential, DefaultAzureCredential
from dotenv import load_dotenv

from acs.caller import OutboundCall

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("phonecalling")

async def create_app():

    load_dotenv()

    app = web.Application()

    if (os.environ.get("ACS_SOURCE_NUMBER") is not None and
        os.environ.get("ACS_CONNECTION_STRING") is not None):
        caller = OutboundCall(
            os.environ.get("ACS_SOURCE_NUMBER"),
            os.environ.get("ACS_CONNECTION_STRING"),
            os.environ.get("CONTAINER_APP_HOSTNAME") + "/acs",
        )
        caller.attach_to_app(app, "/acs")


    # Serve static files and index.html
    current_directory = Path(__file__).parent  # Points to 'app' directory
    static_directory = current_directory / 'static'

    # Ensure static directory exists
    if not static_directory.exists():
        raise FileNotFoundError("Static directory not found at expected path: {}".format(static_directory))

    # Serve index.html at root
    async def index(request):
        return web.FileResponse(static_directory / 'index.html')

    async def call(request):
        if (caller is not None):

            payload = await request.json()
            number = payload.get('number')
            if number is None:
                return web.Response(text="Missing 'number' in request", status=400)
            text = payload.get('text')
            if text is None:
                return web.Response(text="Missing 'text' in request", status=400)
            await caller.call(target_number=number, text=text)
            return web.Response(text="Created outbound call")
        else:
            return web.Response(text="Outbound calling is not configured")

    app.router.add_get('/', index)
    app.router.add_static('/static/', path=str(static_directory), name='static')
    app.router.add_post('/call', call)

    return app

if __name__ == "__main__":
    host = os.environ.get("HOST", "localhost")
    port = int(os.environ.get("PORT", 8765))
    web.run_app(create_app(), host=host, port=port)
