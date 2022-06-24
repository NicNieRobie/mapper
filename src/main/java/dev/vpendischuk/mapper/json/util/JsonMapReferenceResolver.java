package dev.vpendischuk.mapper.json.util;

import dev.vpendischuk.mapper.json.exceptions.CyclicReferenceException;
import dev.vpendischuk.mapper.json.types.JsonObject;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a common interface for map reference resolvers -
 *   classes responsible for handling references in object models
 *   marshalled/unmarshalled by {@link dev.vpendischuk.mapper.json.JsonMapper} instances.
 */
public interface JsonMapReferenceResolver {
    /**
     * Registers a {@link JsonObject} model representation of the {@code obj} object.
     * <p>
     * Handles situations when multiple models represent the same object and thus can be
     *   bound together via references.
     * <p>
     * If the model is not the first model of {@code obj} object registered,
     *   additional service fields '$id' is added to the original model
     *
     * @param obj object represented by the {@code model}.
     * @param model model that represents the {@code obj} in the JSON object model tree.
     */
    void registerObjectModel(Object obj, JsonObject model);

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
    void registerModelRootObject(Object obj) throws CyclicReferenceException;

    /**
     * Unregisters the object {@code obj}, that was previously registered as
     *   a root object of the model tree.
     *
     * @param obj object to be unregistered.
     */
    void unregisterModelRootObject(Object obj);

    /**
     * Registers a reference to an object {@code obj}, parsed from a JSON model,
     *   in the reference pool with a unique {@code id} ID, that was obtained from its model,
     *   for use by models that are referencing the same ID.
     *
     * @param id the unique reference id of the object.
     * @param obj reference to the object.
     */
    void registerObjectReference(UUID id, AtomicReference<Object> obj);

    /**
     * Gets an object reference registered for specified {@code id}.
     *
     * @param id object reference ID.
     * @return object that is referenced with {@code id} or
     *         null if referenced object was not registered.
     */
    Object getObjectReference(UUID id);
}
