package com.hexmeet.hjt.sdk;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SvcConferenceState {
    public static final String STATE_EVENT_UPDATE_STATUS = "CONF_STATUS_UPDATE";
    public static final String STATE_EVENT_MESSAGE_OVERLAY = "message_overlay";

    public static final String STATE_CONTENT_TYPE_MESSAGE = "message_overlay";

    public static final String STATE_ACTION_CAMERA_OPEN = "camera_opened";
    public static final String STATE_ACTION_CAMERA_CLOSE = "camera_closed";
    public static final String STATE_ACTION_MIC_MUTE = "mic_muted";
    public static final String STATE_ACTION_MIC_UNMUTE = "mic_unmuted";
    public static final String STATE_ACTION_REMOTE_MUTED = "remote_muted";
    public static final String STATE_ACTION_REMOTE_UNMUTED = "remote_unmuted";
    public static final String STATE_ACTION_ENDPOINT_ADDED = "endpoint_added";
    public static final String STATE_ACTION_ENDPOINT_DISCONNECTED = "endpoint_dropped";
    public static final String STATE_ACTION_ENDPOINT_CONNECTED = "endpoint_connected";

    public static final String STATE_ACTION_ENABLE_CHANGE_LAYOUT = "enable_change_layout";
    public static final String STATE_ACTION_DISABLE_CHANGE_LAYOUT = "disable_change_layout";

    /**
     * method : INFO
     * from : 1009
     * to : 13300133000
     * callid : 8f5619be-4bdd-4181-a038-70c1c3289ce4
     * content :
     * content-type :
     * contact : {"uid":"1","deviceid":"10319"}
     * cseq : {"sequence":"1","method":"INFO"}
     * event : CONF_STATUS_UPDATE
     * conf_status : {"action":"endpoint_added","participants":["8930"]}
     */

    private String method;
    private String from;
    private String to;
    private String callid;
    private String content;
    @SerializedName("content-type")
    private String contenttype;
    private ContactBean contact;
    private CseqBean cseq;
    private String event;
    private ConfStatusBean conf_status;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCallid() {
        return callid;
    }

    public void setCallid(String callid) {
        this.callid = callid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContenttype() {
        return contenttype;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public ContactBean getContact() {
        return contact;
    }

    public void setContact(ContactBean contact) {
        this.contact = contact;
    }

    public CseqBean getCseq() {
        return cseq;
    }

    public void setCseq(CseqBean cseq) {
        this.cseq = cseq;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public ConfStatusBean getConf_status() {
        return conf_status;
    }

    public void setConf_status(ConfStatusBean conf_status) {
        this.conf_status = conf_status;
    }

    public static class ContactBean {
        /**
         * uid : 1
         * deviceid : 10319
         */

        private String uid;
        private String deviceid;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getDeviceid() {
            return deviceid;
        }

        public void setDeviceid(String deviceid) {
            this.deviceid = deviceid;
        }
    }

    public static class CseqBean {
        /**
         * sequence : 1
         * method : INFO
         */

        private String sequence;
        private String method;

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    public static class ConfStatusBean {
        /**
         * action : endpoint_added
         * participants : ["8930"]
         */

        private String action;
        private ArrayList<String> participants;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public ArrayList<String> getParticipants() {
            return participants;
        }

        public void setParticipants(ArrayList<String> participants) {
            this.participants = participants;
        }
    }
}
