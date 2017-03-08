package com.rtlabs.reqtool.ui.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.capra.core.adapters.ArtifactMetaModelAdapter;
import org.eclipse.capra.core.adapters.Connection;
import org.eclipse.capra.core.adapters.TraceMetaModelAdapter;
import org.eclipse.capra.core.handlers.IAnnotateArtifact;
import org.eclipse.capra.core.handlers.IArtifactHandler;
import org.eclipse.capra.core.helpers.ExtensionPointHelper;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import com.rtlabs.reqtool.model.requirements.Artifact;
import com.rtlabs.reqtool.model.requirements.RequirementsPackage;
import com.rtlabs.reqtool.model.requirements.RequirementsPackage.Literals;
import com.rtlabs.reqtool.model.requirements.Traceable;

public class TraceMetaModelAdapterImpl implements TraceMetaModelAdapter {

	@Override
	public EObject createModel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<EClass> getAvailableTraceTypes(List<EObject> selection) {
		ArrayList<EClass> list = new ArrayList<EClass>();
		list.add(RequirementsPackage.eINSTANCE.getTraceable());
		return list;
	}

	private EditingDomain getEditingDomain(EObject object) {
		return AdapterFactoryEditingDomain.getEditingDomainFor(object);
	}

	@Override
	public EObject createTrace(EClass traceType, EObject traceModel, List<EObject> selection) {
		Traceable parent = null;
		Traceable child = null;
		
		// NOTE: supports 2 objects only
		if (selection.size() != 2)
			return null;

		if (selection.get(0) instanceof Traceable)
			parent = (Traceable) selection.get(0);

		if (selection.get(1) instanceof Traceable)
			child = (Traceable) selection.get(1);

		if (parent == null || child == null)
			return null;
		
		createTrace(parent, child);
		//annotateArtifact(child);
		return null;
	}

	public void createTrace(Traceable parent, Traceable child) {
		EditingDomain parentEditingDomain = getEditingDomain(parent);
		EditingDomain childEditingDomain = getEditingDomain(child);

		if (childEditingDomain == null) {
			childEditingDomain = parentEditingDomain;
		}
		
		Command parentCmd = AddCommand.create(parentEditingDomain, parent, Literals.TRACEABLE__CHILDREN, child);
		Command childCmd = AddCommand.create(childEditingDomain, child, Literals.TRACEABLE__PARENTS, parent);

		if (parentEditingDomain.equals(childEditingDomain)) {
			// Same editing domain, merge these commands into one compound command
			Command compound = parentCmd.chain(childCmd);
			parentEditingDomain.getCommandStack().execute(compound);
		} else {
			parentEditingDomain.getCommandStack().execute(parentCmd);
			childEditingDomain.getCommandStack().execute(childCmd);
		}			
	}

	@Override
	public void deleteTrace(EObject first, EObject second, EObject traceModel) {
		if (!(first instanceof Traceable))
			return;

		if (!(second instanceof Traceable))
			return;

		Traceable _first = (Traceable) first;
		Traceable _second = (Traceable) second;
		deleteTrace(_first, _second);
	}

	public void deleteTrace(Traceable first, Traceable second) {
		EditingDomain firstEditingDomain = getEditingDomain(first);
		EditingDomain secondEditingDomain = getEditingDomain(second);

		Command firstCmd;
		if (first.getChildren().contains(second)) {
			firstCmd = RemoveCommand.create(firstEditingDomain, first, Literals.TRACEABLE__CHILDREN, second);
		} else {
			firstCmd = RemoveCommand.create(firstEditingDomain, first, Literals.TRACEABLE__PARENTS, second);
		}

		Command secondCmd;
		if (second.getChildren().contains(first)) {
			secondCmd = RemoveCommand.create(secondEditingDomain, second, Literals.TRACEABLE__CHILDREN, first);
		} else {
			secondCmd = RemoveCommand.create(secondEditingDomain, second, Literals.TRACEABLE__PARENTS, first);
		}

		if (firstEditingDomain.equals(secondEditingDomain)) {
			// Same editing domain, merge these commands into one compound command
			Command compound = firstCmd.chain(secondCmd);
			firstEditingDomain.getCommandStack().execute(compound);
		} else {
			firstEditingDomain.getCommandStack().execute(firstCmd);
			secondEditingDomain.getCommandStack().execute(secondCmd);
		}
	}

	@Override
	public boolean isThereATraceBetween(EObject first, EObject second, EObject traceModel) {
		if (first instanceof Traceable) {
			Traceable t = (Traceable) first;
			return t.getChildren().contains(second) || t.getParents().contains(second);
		}
		if (second instanceof Traceable) {
			Traceable t = (Traceable) second;
			return t.getChildren().contains(first) || t.getParents().contains(first);
		}
		return false;
	}

	@Override
	public List<Connection> getConnectedElements(EObject element, EObject traceModel) {
		List<Connection> traces = new ArrayList<>();
		if (element instanceof Traceable) {
			Traceable t = (Traceable) element;
			traces.add(new Connection(t, t.getChildren(), t));
		}
		return traces;
	}

	@Override
	public List<Connection> getTransitivelyConnectedElements(EObject element, EObject traceModel) {
		// TODO
		return getConnectedElements(element, traceModel);
	}

}
