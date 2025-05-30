primaryKey(pk, id_iface) ::= <<
<pk.embeddable>
public static class Key extends <id_iface> {
    <pk.fields:field(); separator="\n">

    public Key() {}
    public Key(<pk.fields:{f | <f.type> <f.name>}; separator=", ">) {
        <pk.fields:{f | this.<f.name> = <f.name>;}; separator="\n">
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return <pk.fields:{f | java.util.Objects.equals(this.<f.name>, key.<f.name>)}; separator=" && ">;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(<pk.fields:{f | this.<f.name>}; separator=", ">);
    }

    <pk.accessors:{a | <accessor(a, "Key")>}; separator="\n\n">


<if(pk.onlyString)> <! only has one element !>
    @Override
    public String toString() {
        return this.<first(pk.fields).name>;
    }

    public static Key fromString(String <first(pk.fields).name>) throws java.io.IOException {
        return new Key(<first(pk.fields).name>);
    }
<else>
    public static Key fromString(String data) throws java.io.IOException {
        return <id_iface>.fromBase64Json(data, Key.class);
    }
<endif>
}
>>