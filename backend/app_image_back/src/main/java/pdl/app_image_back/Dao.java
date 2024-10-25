package pdl.app_image_back;

import java.util.Optional;
import java.util.List;

/**
 * The Data Access Object interface for generic types.
 *
 * @param <T> the type of object managed by this DAO.
 */
public interface Dao<T> {
  
  /**
   * Creates a new entity.
   * 
   * @param t The entity to create.
   */
  void create(final T t);

  /**
   * Retrieves an entity by its ID.
   * 
   * @param id The ID of the entity to retrieve.
   * @return An Optional containing the retrieved entity, if found.
   */
  Optional<T> retrieve(final long id);

  /**
   * Retrieves all entities.
   * 
   * @return A List containing all entities.
   */
  List<T> retrieveAll();

  /**
   * Updates an existing entity.
   * 
   * @param t The entity to update.
   * @param params The parameters to update.
   */
  void update(final T t, final String[] params);

  /**
   * Deletes an entity.
   * 
   * @param t The entity to delete.
   */
  void delete(final T t);
}
