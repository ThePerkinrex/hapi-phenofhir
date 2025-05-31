enum(key, value) ::= <<
public static enum <key> {
    // TODO: Create values from int
    <value:{v | <v.a>(<v.b>)}; separator=",\n">
    ;

    private final int value;

    private <key>(int value) {
        this.value = value;
    }

    public int getNumber() {
        return value;
    }

    public static <key> forNumber(int value) {
        switch(value) {
            <value:{v | case <v.b>: return <v.a>;}>
            default: return null;
        }
    }
}
>>