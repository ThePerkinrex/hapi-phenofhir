enum(key, value) ::= <<
public static enum <key> {
    // TODO: Create values from int
    <value:{v | <v.a> /* = <v.b> */}; separator=",\n">
}
>>