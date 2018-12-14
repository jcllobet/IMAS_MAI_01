package cat.urv.imas.utils;

public enum LogCode {
    GENERAL {
        @Override
        public String getColor() {
            return "\u001B[0m";
        }
        @Override
        public String getName() {
            return "GENERAL";
        }
    },
    FATAL {
        @Override
        public String getColor() {
            return "\u001B[31m";
        }
        @Override
        public String getName() {
            return "FATAL ERROR";
        }
    },
    RESET {
        @Override
        public String getColor() {
            return "\u001B[0m";
        }
        @Override
        public String getName() {
            return "";
        }
    },
    ACCEPT_PROP {
        @Override
        public String getColor() {
            return "\u001B[92m";
        }
        @Override
        public String getName() {
            return "ACCEPT_PROP";
        }
    },
    REFUSE {
        @Override
        public String getColor() {
            return "\u001B[31m";
        }
        @Override
        public String getName() {
            return "REFUSE";
        }
    },
    AGREE  {
        @Override
        public String getColor() {
            return "\u001B[32m";
        }
        @Override
        public String getName() {
            return "AGREE";
        }
    },
    CFP   {
        @Override
        public String getColor() {
            return "\u001B[33m";
        }
        @Override
        public String getName() {
            return "CFP";
        }
    },
    INFORM   {
        @Override
        public String getColor() {
            return "\u001B[34m";
        }
        @Override
        public String getName() {
            return "INFORM";
        }
    },
    REQUEST {
        @Override
        public String getColor() {
            return "\u001B[35m";
        }
        @Override
        public String getName() {
            return "REQUEST";
        }
    },
    PROPOSE   {
        @Override
        public String getColor() {
            return "\u001B[36m";
        }
        @Override
        public String getName() {
            return "PROPOSE";
        }
    },
    REJECT_PROP {
        @Override
        public String getColor() {
            return "\u001B[91m";
        }
        @Override
        public String getName() {
            return "REJECT_PROP";
        }
    };

    public abstract String getColor();
    public abstract String getName();
}
