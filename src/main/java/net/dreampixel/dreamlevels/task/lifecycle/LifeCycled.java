package net.dreampixel.dreamlevels.task.lifecycle;

public interface LifeCycled {

    /**
     * @return The life cycle
     */
    int getLifeCycle();

    /**
     * Set the object's life cycle.
     */
    void setLifeCycle(int lifeCycle);

    /**
     * Remove the object.
     */
    void remove();

    /**
     * @return A key used to identify the item
     */
    String getKey();
}
