primaryKey(pk) ::= <<
<pk.embeddable>
public static class Key {
    <pk.fields:field()>
}
>>