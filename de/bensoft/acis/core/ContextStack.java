/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class for managing the context elements.<br>
 * They are sorted by age from youngest to oldest.
 *
 */
public class ContextStack {

	private List<ContextStackItem> mItems = new ArrayList<>(0);
	private int mMaxSize;

	/**
	 * The constructor.
	 * 
	 * @param maxSize
	 *            The maximum size.
	 */
	ContextStack(int maxSize) {
		mMaxSize = maxSize;
	}

	/**
	 * The constructor.
	 * 
	 * @param maxSize
	 *            The maximum size.
	 * @param items
	 *            Items to be added.
	 */
	ContextStack(int maxSize, ContextStackItem[] items) {
		mMaxSize = maxSize;
		mItems.addAll(Arrays.asList(items));
		clean();
	}

	/**
	 * Returns the items.
	 * 
	 * @return The item array sorted from youngest to oldest.
	 */
	public ContextStackItem[] getItems() {
		return mItems.toArray(new ContextStackItem[0]);
	}

	/**
	 * Removes an item at the specified position.
	 * 
	 * @param index
	 *            The position of the item to be removed.
	 */
	void removeItem(int index) {
		mItems.remove(index);
	}

	/**
	 * Updates an item at the specified position. All items are sorted upon
	 * this.
	 * 
	 * @param index
	 *            The position of the item to be updated.
	 * @param newItem
	 *            The new item.
	 */
	void updateItem(int index, ContextStackItem newItem) {
		mItems.set(index, newItem);
		clean();
	}

	/**
	 * Adds an item to the stack. All items are sorted upon this.
	 * 
	 * @param item
	 *            The item to be added.
	 */
	void addItem(ContextStackItem item) {
		mItems.add(item);
		clean();
	}

	/**
	 * Returns the maximum size.
	 * 
	 * @return The maximum size.
	 */
	int getMaximumSize() {
		return mMaxSize;
	}

	/**
	 * Sorts the items and cuts to the maximum size.
	 * 
	 */
	private void clean() {
		mItems.sort(new Comparator<ContextStackItem>() {
			@Override
			public int compare(ContextStackItem arg0, ContextStackItem arg1) {
				if (arg0.getAge() < arg1.getAge())
					return -1;
				if (arg0.getAge() == arg1.getAge())
					return 0;
				return 1;
			}
		});
		mItems = mItems.subList(0, Math.min(mMaxSize, mItems.size()));
	}
}