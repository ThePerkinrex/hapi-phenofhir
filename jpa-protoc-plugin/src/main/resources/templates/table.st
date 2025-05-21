table(package, name, entity, fields, pk, enum_keys, enum_values) ::= <<package <package>;

<entity>
public class <name> {
    <primaryKey(pk)>
    <enum_keys,enum_values:{k,v | <enum(k, v)>}; separator="\n">
    <fields:field(); separator="\n">

    public <name>() {}

    public <name>(<fields:{f | <f.type> <f.name>}; separator=", ">) {
        <fields:{f | this.<f.name> = <f.name>;}; separator="\n">
    }
}
>>