table(package, name, entity, fields, pk, enum_keys, enum_values, accessors, id_iface, with_id_iface, owned) ::= <<package <package>;

<owned>
<entity>
public class <name> implements <with_id_iface>\<<name>.Key\> {
    <primaryKey(pk, id_iface)>
    <enum_keys,enum_values:{k,v | <enum(k, v)>}; separator="\n">
    <fields:field(); separator="\n">

    public <name>() {}

    <! public <name>(<fields:{f | <f.type> <f.name>}; separator=", ">) {
        <fields:{f | this.<f.name> = <f.name>;}; separator="\n">
    } !>
    <accessors:{a | <accessor(a, name)>}; separator="\n\n">

    @Override
    public String toString() {
        return "<name>{" + <fields:{f | "<f.name>=" + this.<f.name> + ", "}; separator=" + "> + "}";
    }

    @Override
    public Class\<Key\> getIdClass() {
        return Key.class;
    }
}
>>