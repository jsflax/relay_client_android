# relay_client_android
Hipchat inspired IRC client

Keep in mind that this is running on Heroku for free so it may take a few
minutes to boot up!

## Messaging

Messages are sent up as raw content and parsed serverside.

See https://github.com/jsflax/relay_server/blob/master/README.md for more info.

The app will then generate a spannable string using the given info to render
the special content.
