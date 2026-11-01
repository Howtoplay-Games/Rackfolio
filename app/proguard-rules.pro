# PortProof release rules. Libraries (Room, Hilt, kotlinx.serialization, Navigation) ship their
# own consumer rules; only app-specific rules belong here.

# Strip verbose/debug/info logging from release builds. Customer data and serial numbers must
# never be logged in any build (see ARCHITECTURE.md, "Logging and privacy").
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
