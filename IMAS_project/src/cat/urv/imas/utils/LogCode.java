package cat.urv.imas.utils;

public enum LogCode {
    RESET  {
        @Override
        public String getCode() {
            return "\u001B[0m";
        }
        @Override
        public String getName() {
            return "";
        }
    },
    ACCEPT_PROPOSAL  {
        @Override
        public String getCode() {
            return "\u001B[30m";
        }
        @Override
        public String getName() {
            return "ACCEPT_PROPOSAL";
        }
    },
    REFUSE    {
        @Override
        public String getCode() {
            return "\u001B[31m";
        }
        @Override
        public String getName() {
            return "REFUSE";
        }
    },
    AGREE  {
        @Override
        public String getCode() {
            return "\u001B[32m";
        }
        @Override
        public String getName() {
            return "AGREE";
        }
    },
    INFORM   {
        @Override
        public String getCode() {
            return "\u001B[34m";
        }
        @Override
        public String getName() {
            return "INFORM";
        }
    },
    REQUEST {
        @Override
        public String getCode() {
            return "\u001B[35m";
        }
        @Override
        public String getName() {
            return "REQUEST";
        }
    },
    PROPOSE   {
        @Override
        public String getCode() {
            return "\u001B[36m";
        }
        @Override
        public String getName() {
            return "PROPOSE";
        }
    },
    CFP   {
        @Override
        public String getCode() {
            return "\u001B[33m";
        }
        @Override
        public String getName() {
            return "CFP";
        }
    },
    REJECT_PROPOSAL  {
        @Override
        public String getCode() {
            return "\u001B[38m";
        }
        @Override
        public String getName() {
            return "REJECT_PROPOSAL";
        }
    };

    public abstract String getCode();
    public abstract String getName();
}
