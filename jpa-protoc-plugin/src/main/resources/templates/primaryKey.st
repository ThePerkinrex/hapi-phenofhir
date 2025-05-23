primaryKey(pk, id_iface) ::= <<
<pk.embeddable>
public static class Key implements <id_iface> {
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


    @Override
    public String toString() {
        return "Key {" + <pk.fields:{f | "<f.name>=" + this.<f.name> + ", "}; separator=" + "> + "}";
    }
}
>>