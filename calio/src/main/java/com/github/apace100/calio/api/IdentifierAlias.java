package com.github.apace100.calio.api;

import com.github.apace100.calio.Calio;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *  A utility class for adding aliases to identifiers (or its namespaces and/or paths).
 */
public class IdentifierAlias {

    public static final Function<Identifier, RuntimeException> NO_ALIAS_EXCEPTION = id -> new RuntimeException("Tried resolving a non-existing alias for identifier \"" + id + "\"");

    protected final Map<Identifier, Identifier> identifierAliases = new HashMap<>();
    protected final Map<String, String> namespaceAliases = new HashMap<>();
    protected final Map<String, String> pathAliases = new HashMap<>();

    /**
     *  Adds an alias to a namespace of an identifier.
     *
     *  @param fromNamespace the alias for the namespace of an identifier
     *  @param toNamespace   the namespace of an identifier to add an alias to
     */
    public void addNamespaceAlias(String fromNamespace, String toNamespace) {

        if (namespaceAliases.containsKey(fromNamespace)) {
            Calio.LOGGER.error("Couldn't add namespace alias \"{}\", as it's already defined!", fromNamespace);
        }

        else {
            namespaceAliases.put(fromNamespace, toNamespace);
        }

    }

    /**
     *  Adds an alias to a path of an identifier.
     *
     *  @param fromPath the alias for the path of an identifier
     *  @param toPath   the path of an identifier to add an alias to
     */
    public void addPathAlias(String fromPath, String toPath) {

        if (namespaceAliases.containsKey(fromPath)) {
            Calio.LOGGER.error("Couldn't add namespace alias \"{}\", as it's already defined!", fromPath);
        }

        else {
            pathAliases.put(fromPath, toPath);
        }

    }

    /**
     *  Adds an alias to a certain identifier.
     *
     *  @param fromId the alias of the identifier
     *  @param toId   the identifier to add an alias to
     */
    public void addAlias(Identifier fromId, Identifier toId) {

        if (identifierAliases.containsKey(fromId)) {
            Calio.LOGGER.error("Couldn't add identifier alias \"{}\", as it's already defined!", fromId);
        }

        else {
            identifierAliases.put(fromId, toId);
        }

    }

    /**
     *  Checks if the specified {@link Identifier id} has an alias.
     *
     *  @param id   the {@link Identifier} to check for aliases on
     *  @return     {@code true} if the specified {@link Identifier id} has an alias
     */
    public boolean hasIdentifierAlias(Identifier id) {
        return identifierAliases.containsKey(id)
            || GlobalIdentifierAlias.INSTANCE.hasIdentifierAlias(id);
    }

    /**
     *  Checks if the specified {@link Identifier id}'s namespace has an alias.
     *
     *  @param id   the {@link Identifier} to check for aliases on
     *  @return     {@code true} if the specified {@link Identifier id}'s namespace has an alias
     */
    public boolean hasNamespaceAlias(Identifier id) {
        return namespaceAliases.containsKey(id.getNamespace())
            || GlobalIdentifierAlias.INSTANCE.hasNamespaceAlias(id);
    }

    /**
     *  Checks if the specified {@link Identifier id}'s path has an alias.
     *
     *  @param id   the {@link Identifier} to check for aliases on
     *  @return     {@code true} if the specified {@link Identifier id}'s path has an alias
     */
    public boolean hasPathAlias(Identifier id) {
        return pathAliases.containsKey(id.getPath())
            || GlobalIdentifierAlias.INSTANCE.hasPathAlias(id);
    }

    /**
     *  Checks if the specified {@link Identifier id} has an alias (in general.)
     *
     *  @param id   the {@link Identifier} to check for aliases on
     *  @return     {@code true} if the specified {@link Identifier id} has an alias (in general.)
     */
    public boolean hasAlias(Identifier id) {
        return hasIdentifierAlias(id)
            || hasNamespaceAlias(id)
            || hasPathAlias(id);
    }

    /**
     *  <p>Attempts to resolve the aliases for the specified {@link Identifier id} until {@link Predicate<Identifier> resolvedPredicate} evaluates
     *  to {@code true}.</p>
     *
     *  @param id                   the {@link Identifier} to resolve the aliases of
     *  @param resolvedPredicate    the condition which determines if the aliases of the {@link Identifier id} has been successfully resolved
     *
     *  @return                     the resolved {@link Identifier}, or the passed {@link Identifier id} if it has no aliases
     */
    public Identifier resolveAliasUntil(Identifier id, Predicate<Identifier> resolvedPredicate) {

        if (!hasAlias(id)) {
            return id;
        }

        Identifier aliasedId;
        Resolver[] resolvers = Resolver.values();

        for (Resolver resolver : resolvers) {

            aliasedId = resolver.resolve(this, id);

            if (resolvedPredicate.test(aliasedId)) {
                return aliasedId;
            }

        }

        return id;

    }

    /**
     *  Resolves the identifier alias for the specified {@link Identifier id}.
     *
     *  @param id       the {@link Identifier} to resolve the identifier aliases of
     *  @param strict   determines whether to throw an exception if no aliases are found
     *
     *  @return         the resolved {@link Identifier}, or the passed {@link Identifier id} if {@code strict} is {@code false}
     */
    public Identifier resolveIdentifierAlias(Identifier id, boolean strict) {

        if (strict && !hasIdentifierAlias(id)) {
            throw NO_ALIAS_EXCEPTION.apply(id);
        }

        return identifierAliases.getOrDefault(id, GlobalIdentifierAlias.INSTANCE.resolveIdentifierAlias(id, strict));

    }

    /**
     *  Resolves the namespace alias for the specified {@link Identifier id}.
     *
     *  @param id       the {@link Identifier} to resolve the namespace aliases of
     *  @param strict   determines whether to throw an exception if no aliases are found
     *
     *  @return         the resolved {@link Identifier}, or the passed {@link Identifier id} if {@code strict} is {@code false}
     */
    public Identifier resolveNamespaceAlias(Identifier id, boolean strict) {

        if (strict && !hasNamespaceAlias(id)) {
            throw NO_ALIAS_EXCEPTION.apply(id);
        }

        String namespace = id.getNamespace();
        String aliasedNamespace = namespaceAliases.getOrDefault(
            namespace,
            GlobalIdentifierAlias.INSTANCE.resolveNamespaceAlias(id, strict).getNamespace()
        );

        return Identifier.of(aliasedNamespace, id.getPath());

    }

    /**
     *  Resolves the path alias for the specified {@link Identifier id}.
     *
     *  @param id       the {@link Identifier} to resolve the path aliases of
     *  @param strict   determines whether to throw an exception if no aliases are found
     *
     *  @return         the resolved {@link Identifier}, or the passed {@link Identifier id} if {@code strict} is {@code false}
     */
    public Identifier resolvePathAlias(Identifier id, boolean strict) {

        if (strict && !hasPathAlias(id)) {
            throw NO_ALIAS_EXCEPTION.apply(id);
        }

        String path = id.getPath();
        String aliasedPath = pathAliases.getOrDefault(
            path,
            GlobalIdentifierAlias.INSTANCE.resolvePathAlias(id, strict).getPath()
        );

        return Identifier.of(id.getNamespace(), aliasedPath);

    }

    public enum Resolver {

        NO_OP((aliases, id) ->
            id),
        IDENTIFIER((aliases, id)->
            aliases.resolveIdentifierAlias(id, false)),
        NAMESPACE((aliases, id) ->
            aliases.resolveNamespaceAlias(id, false)),
        PATH((aliases, id) ->
            aliases.resolvePathAlias(id, false)),
        NAMESPACE_AND_PATH((aliases, id) -> {

            String aliasedNamespace = aliases.resolveNamespaceAlias(id, false).getNamespace();
            String aliasedPath = aliases.resolvePathAlias(id, false).getPath();

            return Identifier.of(aliasedNamespace, aliasedPath);

        });

        private final BiFunction<IdentifierAlias, Identifier, Identifier> resolver;
        Resolver(BiFunction<IdentifierAlias, Identifier, Identifier> resolver) {
            this.resolver = resolver;
        }

        private Identifier resolve(IdentifierAlias aliases, Identifier id) {
            return resolver.apply(aliases, id);
        }

    }

}
