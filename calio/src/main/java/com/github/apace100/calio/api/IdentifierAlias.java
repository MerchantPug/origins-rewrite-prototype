package com.github.apace100.calio.api;

import com.github.apace100.calio.Calio;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Comments for this class. At least I've started on it though :P - Pug
/**
 *  A utility class for adding aliases to identifiers (or its namespaces and/or paths).
 */
public class IdentifierAlias {
    private static final RuntimeException NO_NAMESPACE_ALIAS = new RuntimeException("Tried to resolve a namespace alias for a namespace which didn't have an alias.");
    private static final RuntimeException NO_PATH_ALIAS = new RuntimeException("Tried to resolve a path alias for a path which didn't have an alias.");
    private static final RuntimeException NO_ALIAS = new RuntimeException("Tried to resolve an alias for an identifier which didn't have an alias.");

    private static final Map<Optional<RegistryEntry<Registry<?>>>, Map<Identifier, Identifier>> ALIASED_IDENTIFIERS = new HashMap<>();
    private static final Map<Optional<RegistryEntry<Registry<?>>>, Map<String, String>> ALIASED_NAMESPACES = new HashMap<>();
    private static final Map<Optional<RegistryEntry<Registry<?>>>, Map<String, String>> ALIASED_PATHS = new HashMap<>();

    public enum Priority {
        IDENTIFIER,
        NAMESPACE,
        PATH
    }

    /**
     * Adds a namespace alias globally.
     *
     * @param fromNamespace A namespace to resolve from.
     * @param toNamespace   The namespace to resolve to.
     */
    public static void addGlobalNamespaceAlias(String fromNamespace, String toNamespace) {
        addNamespaceAlias(null, fromNamespace, toNamespace);
    }

    /**
     * Adds a namespace alias for the specified registry, or globally if registry is null.
     *
     * @param registry      A registry to create a namespace alias for. If null, this will act on a global level.
     * @param fromNamespace A namespace to resolve from.
     * @param toNamespace   The namespace to resolve to.
     */
    public static void addNamespaceAlias(@Nullable RegistryEntry<Registry<?>> registry, String fromNamespace, String toNamespace) {

        if (ALIASED_NAMESPACES.containsKey(Optional.ofNullable(registry)) && ALIASED_NAMESPACES.get(Optional.ofNullable(registry)).containsKey(fromNamespace)) {
            Calio.LOGGER.error("[{}] Cannot add alias \"{}\" to namespace \"{}\" in registry \"{}\", as it already exists!", Calio.MODID, fromNamespace, toNamespace, Optional.ofNullable(registry).map(RegistryEntry::getIdAsString).orElse("global"));
            return;
        }

        ALIASED_NAMESPACES.computeIfAbsent(Optional.ofNullable(registry), opt -> new HashMap<>()).put(fromNamespace, toNamespace);

    }

    /**
     * Adds a path alias globally.
     *
     * @param fromPath  A path to resolve from.
     * @param toPath    The path to resolve to.
     */
    public static void addGlobalPathAlias(String fromPath, String toPath) {
        addPathAlias(null, fromPath, toPath);
    }

    /**
     * Adds a namespace alias for the specified registry, or globally if registry is null.
     *
     * @param registry      A registry to create a path alias for. If null, this will act on a global level.
     * @param fromPath      A path to resolve from.
     * @param toPath   The path to resolve to.
     */
    public static void addPathAlias(@Nullable RegistryEntry<Registry<?>> registry, String fromPath, String toPath) {

        if (ALIASED_PATHS.containsKey(Optional.ofNullable(registry)) && ALIASED_PATHS.get(Optional.ofNullable(registry)).containsKey(fromPath)) {
            Calio.LOGGER.error("[{}] Cannot add alias \"{}\" to path \"{}\" in registry \"{}\", as it already exists!", Calio.MODID, fromPath, toPath, Optional.ofNullable(registry).map(RegistryEntry::getIdAsString).orElse("global"));
            return;
        }

        ALIASED_PATHS.computeIfAbsent(Optional.ofNullable(registry), opt -> new HashMap<>()).put(fromPath, toPath);

    }

    /**
     * Adds an identifier alias globally.
     *
     * @param fromId    An identifier to resolve from.
     * @param toId      The identifier to resolve to.
     */
    public static void addGlobalAlias(Identifier fromId, Identifier toId) {
        addAlias(null, fromId, toId);
    }

    /**
     * Adds an identifier alias for the specified registry, or globally if registry is null.
     *
     * @param registry  A registry to create an identifier alias for. If null, this will act on a global level.
     * @param fromId    An identifier to  resolve from.
     * @param toId      The identifier to resolve to.
     */
    public static void addAlias(@Nullable RegistryEntry<Registry<?>> registry, Identifier fromId, Identifier toId) {

        if (identifierHasAlias(registry, fromId)) {
            Calio.LOGGER.error("[{}] Cannot add alias \"{}\" to identifier \"{}\" in registry \"{}\", as it already exists!", Calio.MODID, fromId, toId, Optional.ofNullable(registry).map(RegistryEntry::getIdAsString).orElse("global"));
            return;
        }

        ALIASED_IDENTIFIERS.computeIfAbsent(Optional.ofNullable(registry), opt -> new HashMap<>()).put(fromId, toId);

    }

    public static boolean identifierHasAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        Optional<RegistryEntry<Registry<?>>> global = Optional.empty();
        Optional<RegistryEntry<Registry<?>>> optionalRegistry = Optional.of(registry);
        return ALIASED_IDENTIFIERS.containsKey(optionalRegistry) && ALIASED_IDENTIFIERS.get(optionalRegistry).containsKey(id)
                || ALIASED_IDENTIFIERS.containsKey(global) && ALIASED_IDENTIFIERS.get(global).containsKey(id);
    }

    public static boolean namespaceHasAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        return namespaceHasAlias(registry, id.getNamespace());
    }

    public static boolean namespaceHasAlias(RegistryEntry<Registry<?>> registry, String namespace) {
        Optional<RegistryEntry<Registry<?>>> global = Optional.empty();
        Optional<RegistryEntry<Registry<?>>> optionalRegistry = Optional.of(registry);
        return ALIASED_NAMESPACES.containsKey(optionalRegistry) && ALIASED_NAMESPACES.get(optionalRegistry).containsKey(namespace)
                || ALIASED_NAMESPACES.containsKey(global) && ALIASED_NAMESPACES.get(global).containsKey(namespace);
    }

    public static boolean pathHasAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        return pathHasAlias(registry, id.getPath());
    }

    public static boolean pathHasAlias(RegistryEntry<Registry<?>> registry, String path) {
        Optional<RegistryEntry<Registry<?>>> global = Optional.empty();
        Optional<RegistryEntry<Registry<?>>> optionalRegistry = Optional.of(registry);
        return ALIASED_PATHS.containsKey(optionalRegistry) && ALIASED_PATHS.get(optionalRegistry).containsKey(path)
                || ALIASED_PATHS.containsKey(global) && ALIASED_PATHS.get(global).containsKey(path);
    }

    public static boolean hasAlias(@Nullable RegistryEntry<Registry<?>> registry, Identifier id) {
        return identifierHasAlias(registry, id)
                || (namespaceHasAlias(registry, id) || pathHasAlias(registry, id));
    }

    private static Identifier resolveIdentifierAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        if (!namespaceHasAlias(registry, id)) {
            throw NO_ALIAS;
        } else {
            Optional<RegistryEntry<Registry<?>>> global = Optional.empty();
            if (ALIASED_IDENTIFIERS.containsKey(global) && ALIASED_IDENTIFIERS.get(global).containsKey(id)) {
                return ALIASED_IDENTIFIERS.get(global).get(id);
            }
            return ALIASED_IDENTIFIERS.get(Optional.of(registry)).get(id);
        }
    }

    private static Identifier resolveNamespaceAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        if (!namespaceHasAlias(registry, id)) {
            throw NO_NAMESPACE_ALIAS;
        } else {
            Optional<RegistryEntry<Registry<?>>> global = Optional.empty();
            if (ALIASED_NAMESPACES.containsKey(global) && ALIASED_NAMESPACES.get(global).containsKey(id.getNamespace())) {
                return new Identifier(ALIASED_NAMESPACES.get(global).get(id.getNamespace()), id.getPath());
            }
            return new Identifier(ALIASED_NAMESPACES.get(Optional.of(registry)).get(id.getNamespace()), id.getPath());
        }
    }

    private static Identifier resolvePathAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        if (!pathHasAlias(registry, id)) {
            throw NO_PATH_ALIAS;
        } else {
            Optional<RegistryEntry<Registry<?>>> global = Optional.empty();
            if (ALIASED_PATHS.containsKey(global) && ALIASED_PATHS.get(global).containsKey(id.getPath())) {
                return new Identifier(id.getNamespace(), ALIASED_PATHS.get(global).get(id.getPath()));
            }
            return new Identifier(id.getNamespace(), ALIASED_PATHS.get(Optional.of(registry)).get(id.getPath()));
        }
    }

    public static Identifier resolveAlias(RegistryEntry<Registry<?>> registry, Identifier id) {
        return resolveAlias(registry, id, null);
    }

    /**
     * Resolves an alias for the specified identifier within the specified registry.
     *
     * @param registry          A registry linked to rhe resolution.
     * @param id                An identifier to resolve.
     * @param specifiedPriority A specified priority to prioritise first,
     *                          will prioritise in the enum order if null.
     * @return                  The resolved identifier, or the original if a resolution could
     *                          not be found.
     */
    public static Identifier resolveAlias(RegistryEntry<Registry<?>> registry, Identifier id, @Nullable Priority specifiedPriority) {

        Identifier aliasedId = new Identifier(id.getNamespace(), id.getPath());
        List<Priority> priorities = Arrays.stream(Priority.values())
                .sorted(Enum::compareTo)
                .collect(Collectors.toCollection(LinkedList::new));

        if (specifiedPriority != null) {
            priorities.remove(specifiedPriority);
            aliasedId = resolve(registry, aliasedId, specifiedPriority);
        }

        for (Priority priority : priorities) {
            aliasedId = resolve(registry, aliasedId, priority);
        }

        return aliasedId;

    }

    private static Identifier resolve(RegistryEntry<Registry<?>> registry, Identifier id, Priority priority) {
        return switch (priority) {
            case IDENTIFIER -> identifierHasAlias(registry, id) ? resolveIdentifierAlias(registry, id) : id;
            case NAMESPACE -> namespaceHasAlias(registry, id) ? resolveNamespaceAlias(registry, id) : id;
            case PATH -> pathHasAlias(registry, id) ? resolvePathAlias(registry, id): id;
        };
    }
}
