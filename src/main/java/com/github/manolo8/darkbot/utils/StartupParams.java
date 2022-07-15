package com.github.manolo8.darkbot.utils;

import eu.darkbot.api.API;
import eu.darkbot.util.function.ThrowingFunction;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class StartupParams implements API.Singleton {
    private static final String COMMAND_PREFIX = "-";

    public void updateSidAndServer(String sid, String server) {
        Properties login = (Properties) startupParams.get(LaunchArg.LOGIN);
        Path path = new File(login.getProperty("SourceFile")).toPath();
        login.setProperty(PropertyKey.SERVER.toString(), server);
        login.setProperty(PropertyKey.SID.toString(), sid);

        try {
            Files.write(path, Arrays.stream(PropertyKey.values())
                    .filter(x -> login.containsKey(x.toString()))
                    .map(x -> x + "=" + getAutoLoginValue(x))
                    .collect(Collectors.toList()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum LaunchArg {
        /**
         * Auto-login without a login pop-up, requires a path to a properties file
         * with a username and either a password or a master-password.
         * Example usage: {@code -login C:\Users\Owner\login.properties}
         */
        LOGIN(StartupParams::loadLoginProperties),
        START, /** Auto-start the bot */
        NO_OP, /** Run the bot in no-op mode (no-op api) */
        CONFIG(s -> s), /** Start the bot with a specific config */
        HIDE, /** If the bot should hide api window on start */
        NO_WARN; /** Disable warnings about unsupported java version */

        private final ThrowingFunction<String, ?, Exception> parser;

        LaunchArg() {
            this(null);
        }

        LaunchArg(ThrowingFunction<String, ?, Exception> parser) {
            this.parser = parser;
        }

        public static LaunchArg of(String str) {
            while (str.startsWith(COMMAND_PREFIX)) str = str.substring(1);
            try {
                return LaunchArg.valueOf(str.toUpperCase(Locale.ROOT).replace("-", "_"));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public Object parse(String param) {
            if (parser == null) return true;
            try {
                return parser.apply(param);
            } catch (Exception e) {
                System.err.println("Failed to parse parameter for argument: " + this);
                e.printStackTrace();
                return null;
            }
        }
    }

    public enum PropertyKey {
        USERNAME, PASSWORD, MASTER_PASSWORD, SERVER, SID;
        @Override
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }


    private final Map<LaunchArg, Object> startupParams = new HashMap<>();

    public StartupParams(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String strArgument = args[i];
            LaunchArg arg = LaunchArg.of(strArgument);
            if (arg == null) {
                System.err.println("Unknown startup argument: " + strArgument + " , ignoring argument.");
                continue;
            }
            if (arg.parser != null) i++;
            if (i >= args.length) {
                System.err.println("Missing required argument for " + strArgument);
                break;
            }
            startupParams.put(arg, arg.parse(args[i]));
        }
    }

    /* Auto login */
    private static Properties loadLoginProperties(String path) throws IOException {
        Properties p = new Properties();
        p.setProperty("SourceFile", path);
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        System.out.println("Loaded startup properties file");
        return p;
    }

    public String getAutoLoginValue(PropertyKey key) {
        Properties p = (Properties) startupParams.get(LaunchArg.LOGIN);
        return p.getProperty(key.toString());
    }

    public char[] getAutoLoginMasterPassword() {
        String masterPassword = getAutoLoginValue(PropertyKey.MASTER_PASSWORD);
        return masterPassword == null ? null : masterPassword.toCharArray();
    }

    public void clearAutoLoginProp(PropertyKey key) {
        ((Properties) startupParams.get(LaunchArg.LOGIN)).setProperty(key.toString(), "");
    }

    /* Other params */
    public boolean has(LaunchArg arg) {
        return startupParams.containsKey(arg);
    }

    public boolean getAutoLogin() {
        return has(LaunchArg.LOGIN);
    }

    public boolean getAutoStart() {
        return has(LaunchArg.START);
    }

    public boolean useNoOp() {
        return has(LaunchArg.NO_OP);
    }

    public String getStartConfig() {
        return (String) startupParams.getOrDefault(LaunchArg.CONFIG, null);
    }

    public boolean getAutoHide() {
        return has(LaunchArg.HIDE);
    }

    @Override
    public String toString() {
        return "StartupParams{" + startupParams.entrySet().stream()
                .map(e -> e.getKey().toString() + "= " + e.getValue().toString())
                .collect(Collectors.joining(","));
    }
}
