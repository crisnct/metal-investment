package com.investment.metal.infrastructure.service;

/**
 * Infrastructure enum for mail templates.
 * Follows Clean Architecture principles by keeping mail infrastructure concerns separate.
 */
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
    },

    DELETE_ACCOUNT_PREPARATION {
        @Override
        public String getFilename() {
            return "mail-template-delete-account-preparation.html";
        }
    };

    public abstract String getFilename();
}
