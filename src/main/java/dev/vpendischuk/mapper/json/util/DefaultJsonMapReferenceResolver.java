package dev.vpendischuk.mapper.json.util;

import dev.vpendischuk.mapper.json.exceptions.CyclicReferenceException;
import dev.vpendischuk.mapper.json.types.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation of the {@link JsonMapReferenceResolver} interface,
 *   used by default by all {@link dev.vpendischuk.mapper.json.JsonMapper} instances
 *   for reference resolving.
 */
public class DefaultJsonMapReferenceResolver implements JsonMapReferenceResolver {
    // Map that stores entries of type 'object - object's JSON model representation'.
    private final HashMap<Object, JsonObject> objectModelMapping;
    // Map that stores unique references to objects.
    private final IdentityHashMap<UUID, Object> referenceMap;
    // Map that stores registered object references for unmarshalling.
    private final HashMap<UUID, AtomicReference<Object>> referencePool;
    // Map that stores registered root objects.
    private final Set<Object> rootObjects;

    /**
     * Initializes a new {@link DefaultJsonMapReferenceResolver} instance.
     */
    public DefaultJsonMapReferenceResolver() {
        objectModelMapping = new HashMap<>();
        referenceMap = new IdentityHashMap<>();
        referencePool = new HashMap<>();
        rootObjects = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /**
     * Registers a {@link JsonObject} model representation of the {@code obj} object.
     * <p>
     * Handles situations when multiple models represent the same object and thus can be
     *   bound together via references.
     * <p>
     * If the model is not the first model of {@code obj} object registered,
     *   additional service field '$ref' with a unique ID is added to every
     *   model representing the same object.
     *
     * @param obj object represented by the {@code model}.
     * @param model model that represents the {@code obj} in the JSON object model tree.
     */
    @Override
    public void registerObjectModel(Object obj, JsonObject model) {
        if (!referenceMap.containsValue(obj)) {
            referenceMap.put(UUID.randomUUID(), obj);
            objectModelMapping.put(obj, model);
        } else {
            UUID objectId = null;

            for (final Map.Entry<UUID, Object> entry : referenceMap.entrySet()) {
                if (obj == entry.getValue()) {
                    objectId = entry.getKey();
                    break;
                }
            }

            if (objectId != null) {
                objectModelMapping.get(obj).addKeyValuePair("$ref", objectId.toString());
                model.addKeyValuePair("$ref", objectId.toString());
            }
        }
    }

    /**
     * Registers the {@code obj} object as one of the current root objects -
     *   objects that are at the current 'root' of the branch of the object relationship tree
     *   that is currently being parsed and translated into a JSON model.
     * <p>
     * Root objects are expected not to be the part of the currently parsed branch
     *   as a method of excluding cases of cyclic references in the object relationship tree.
     *
     * @param obj object to be registered as part of the root.
     * @throws CyclicReferenceException if the root object is encountered while
     *                                  still being registered by the resolver.
     */
    @Override
    public void registerModelRootObject(Object obj) throws CyclicReferenceException {
        if (rootObjects.contains(obj)) {
            throw new CyclicReferenceException("[JSON Reference Resolver Error] " +
                    "Reference cycle detected in object model tree.");
        }

        rootObjects.add(obj);
    }

    /**
     * Unregisters the object {@code obj}, that was previously registered as
     *   a root object of the model tree.
     *
     * @param obj object to be unregistered.
     */
    @Override
    public void unregisterModelRootObject(Object obj) {
        rootObjects.remove(obj);
    }

    /**
     * Registers a reference to an object {@code obj}, parsed from a JSON model,
     *   in the reference pool with a unique {@code id} ID, that was obtained from its model,
     *   for use by models that are referencing the same ID.
     *
     * @param id the unique reference id of the object.
     * @param obj reference to the object.
     */
    @Override
    public void registerObjectReference(UUID id, AtomicReference<Object> obj) {
        referencePool.put(id, obj);
    }

    /**
     * Gets an object reference registered for specified {@code id}.
     *
     * @param id object reference ID.
     * @return object that is referenced with {@code id} or
     *         null if referenced object was not registered.
     */
    @Override
    public Object getObjectReference(UUID id) {
        AtomicReference<Object> reference = referencePool.get(id);

        if (reference == null) {
            return null;
        }

        return reference.get();
    }
}
