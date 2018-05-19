package com.perezjquim.noallerg;

public enum ToastMessages
{
    PARSING_ERROR("Server parsing error - try again later"),
    NETWORK_ERROR("Connection error - make sure you have an internet connection"),
    UNHANDLED_ERROR("Unhandled error - restart the app and try again later"),
    GPS_INIT("Going to current location.."),
    GPS_SUCCESS("You have been relocated to your current location!"),
    GPS_ERROR("GPS Error - make sure you have GPS enabled in your device"),
    UPDATE_MARKERS_INIT("Updating markers.."),
    UPDATE_MARKERS_SUCCESS("Markers updated successfully!");

    final String message;

    ToastMessages(final String message)
    {
        this.message = message;
    }
}
