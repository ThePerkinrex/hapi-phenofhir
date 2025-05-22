accessor(a, c) ::= <<
public <a.type> <a.getterName>() {
    return this.<a.name>;
}

public <c> <a.setterName>(<a.type> <a.name>) {
    this.<a.name> = <a.name>;
    <a.toNull:{f | this.<f.name> = null;}; separator="\n">
    return this;
}
>>