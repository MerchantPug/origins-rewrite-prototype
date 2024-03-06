package com.github.apace100.calio.api;

import net.minecraft.util.Identifier;

/**
 *  <p>Like {@link IdentifierAlias}, but globally instead of per-instance. This is internally used when resolving aliases in {@link IdentifierAlias},
 *  where per-instance aliases precede global aliases.</p>
 *
 *  <p>TODO: There's probably a better way of doing this... -eggohito</p>
 */
public class GlobalIdentifierAlias extends IdentifierAlias {

    public static final GlobalIdentifierAlias INSTANCE = new GlobalIdentifierAlias();

    private GlobalIdentifierAlias() {

    }

    @Override
    public boolean hasIdentifierAlias(Identifier id) {
        return identifierAliases.containsKey(id);
    }

    @Override
    public boolean hasNamespaceAlias(Identifier id) {
        return namespaceAliases.containsKey(id.getNamespace());
    }

    @Override
    public boolean hasPathAlias(Identifier id) {
        return pathAliases.containsKey(id.getPath());
    }

    @Override
    public Identifier resolveIdentifierAlias(Identifier id, boolean strict) {

        if (strict && !hasIdentifierAlias(id)) {
            throw NO_ALIAS_EXCEPTION.apply(id);
        }

        return identifierAliases.getOrDefault(id, id);

    }

    @Override
    public Identifier resolveNamespaceAlias(Identifier id, boolean strict) {

        if (strict && !hasNamespaceAlias(id)) {
            throw NO_ALIAS_EXCEPTION.apply(id);
        }

        String namespace = id.getNamespace();
        String aliasedNamespace = namespaceAliases.getOrDefault(namespace, namespace);

        return Identifier.of(aliasedNamespace, id.getPath());

    }

    @Override
    public Identifier resolvePathAlias(Identifier id, boolean strict) {

        if (strict && !hasPathAlias(id)) {
            throw NO_ALIAS_EXCEPTION.apply(id);
        }

        String path = id.getPath();
        String aliasedPath = pathAliases.getOrDefault(path, path);

        return Identifier.of(id.getNamespace(), aliasedPath);

    }

}
