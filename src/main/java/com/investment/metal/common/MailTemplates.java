package com.investment.metal.common;

public enum MailTemplates {
    VERIFICATION_CODE {
        @Override
        public String getFilename() {
            return "mail-template-code.html";
        }
    },

    ALERT {
        @Override
        public String getFilename() {
            return "mail-template-alert-trigger.html";
        }
    },

    STATUS {
        @Override
        public String getFilename() {
            return "mail-template-status.html";
        }
    },

    STATUS_PART {
        @Override
        public String getFilename() {
            return "mail-template-status-part.html";
        }
    };

    public abstract String getFilename();
}