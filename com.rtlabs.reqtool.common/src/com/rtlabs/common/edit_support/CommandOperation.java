package com.rtlabs.common.edit_support;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Used to enable clients to run a command. Passes arguments that are required for most commands. 
 */
public interface CommandOperation<T extends EObject> {
	void run(EditContext editContext, IObservableValue<T> containingEntity, EStructuralFeature containingFeature);
}
