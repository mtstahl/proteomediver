package de.tum.bio.analysis;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import application.Main;
import application.MainController;
import de.tum.bio.analysis.gui.AnalysisComponentOpener;
import de.tum.bio.proteomics.FastaFile;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.proteomics.StatisticsFile;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * This class handles a set different analyses components.
 * @author Matthias Stahl
 *
 */

public final class AnalysisHandler {
	
	private static final AnalysisHandler analysisHandler = new AnalysisHandler();
	
	private ObservableMap<Integer, Analysis> analysisCollection = FXCollections.observableMap(new HashMap<Integer, Analysis>());
	private MainController controller;
	
	private IntegerProperty selectedAnalysisId = new SimpleIntegerProperty(-1);
	private IntegerProperty selectedPeptideIdId = new SimpleIntegerProperty(-1);
	private IntegerProperty selectedProteinGroupId = new SimpleIntegerProperty(-1);
	private IntegerProperty selectedPeptideId = new SimpleIntegerProperty(-1);
	
	private AnalysisHandler() {
		// empty
	}
	
	public static AnalysisHandler getInstance() {
		return analysisHandler;
	}
	
	public void setController(MainController controller) {
		this.controller = controller;
		analysisCollection.addListener(new MapChangeListener<Integer, Analysis>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends Integer, ? extends Analysis> change) {
				buildTreeView(controller.getTreeView());
			}
		});
	}
	
	public void addItem(int analysisId, AnalysisComponentType analysisComponentType, Main mainApp) {
		Analysis analysis;
		boolean newAnalysis = false;
		
		if (analysisId < 0) {
			analysisId = getNextId(analysisCollection.keySet());
		}
		
		if (!analysisCollection.containsKey(analysisId)) {
			analysis = new Analysis(analysisId);
			analysis.getPeptideIds().addListener(new MapChangeListener<Integer, PeptideId>() {
				@Override
				public void onChanged(MapChangeListener.Change<? extends Integer, ? extends PeptideId> change) {
					buildTreeView(controller.getTreeView());
				}
			});
			analysis.getStatisticsFiles().addListener(new MapChangeListener<Integer, StatisticsFile>() {
				@Override
				public void onChanged(MapChangeListener.Change<? extends Integer, ? extends StatisticsFile> change) {
					buildTreeView(controller.getTreeView());
				}
			});
			analysis.getFastaFiles().addListener(new MapChangeListener<Integer, FastaFile>() {
				@Override
				public void onChanged(MapChangeListener.Change<? extends Integer, ? extends FastaFile> change) {
					buildTreeView(controller.getTreeView());
				}
			});
			newAnalysis = true;
		} else {
			analysis = analysisCollection.get(analysisId);
		}
		
		if (analysisComponentType == null) {
			// Only new empty analysis should be created
			analysisCollection.put(analysisId, analysis);
		} else {
			analysis.setDataAssigned(false);
			if (newAnalysis) {
				analysis.dataAssignedProperty().addListener((c, o, n) -> {
					analysisCollection.put(analysis.getId(), analysis);
				});
			}
			
			switch (analysisComponentType) {
				case MaxQuant:
					AnalysisComponentOpener.getMQCollection(mainApp, analysis);
					break;
				case Perseus:
					AnalysisComponentOpener.getPerseusCollection(mainApp, analysis);
					break;
				case Fasta:
					AnalysisComponentOpener.getFastaCollection(mainApp, analysis);
					break;
				default:
					break;
			}
		}
	}
	
	public void newAnalysis() {
		addItem(-1, null, null);
	}
	
	public void removeAnalysis(int id) {
		analysisCollection.remove(id);
	}
	
	public ObservableMap<Integer, Analysis> getAllAnalyses() {
		return analysisCollection;
	}
	
	public Analysis getAnalysis(int id) {
		return analysisCollection.get(id);
	}
	
	public Analysis getAnalysis() {
		return analysisCollection.get(selectedAnalysisId.get());
	}
	
	public boolean isEmpty() {
		return analysisCollection.isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AnalysisComponent> void buildTreeView(TreeView<T> treeView) {
		TreeItem<T> selectedItem = treeView.getSelectionModel().getSelectedItem();
		TreeItem<T> root = new TreeItem<T>();
		for (Entry<Integer, Analysis> analysis : getAllAnalyses().entrySet()) {
			TreeItem<T> analysisItem = (TreeItem<T>) new TreeItem<Analysis>(analysis.getValue());
			for (Entry<Integer, PeptideId> peptideId : analysis.getValue().getPeptideIds().entrySet()) {
				TreeItem<T> peptideIdItem = (TreeItem<T>) new TreeItem<PeptideId>(peptideId.getValue());
				analysisItem.getChildren().add(peptideIdItem);
			}
			for (Entry<Integer, StatisticsFile> statisticsFile : analysis.getValue().getStatisticsFiles().entrySet()) {
				TreeItem<T> statisticsFileItem = (TreeItem<T>) new TreeItem<StatisticsFile>(statisticsFile.getValue());
				analysisItem.getChildren().add(statisticsFileItem);
			}
			for (Entry<Integer, FastaFile> fastaFile : analysis.getValue().getFastaFiles().entrySet()) {
				TreeItem<T> fastaFileItem = (TreeItem<T>) new TreeItem<FastaFile>(fastaFile.getValue());
				analysisItem.getChildren().add(fastaFileItem);
			}
			if (!analysisItem.isLeaf()) {
				analysisItem.setExpanded(true);
			}
			root.getChildren().add(analysisItem);
		}
		treeView.setRoot(root);
		treeView.getSelectionModel().select(selectedItem);
	}
	
	public void setSelectedPeptideIdId(int id) {
		this.selectedPeptideIdId.set(id);
	}
	
	public int getSelectedPeptideIdId() {
		return selectedPeptideIdId.get();
	}
	
	public IntegerProperty selectedPeptideIdIdProperty() {
		return selectedPeptideIdId;
	}
	
	public void setSelectedAnalysisId(int id) {
		this.selectedAnalysisId.set(id);
	}
	
	public int getSelectedAnalysisId() {
		return selectedAnalysisId.get();
	}
	
	public IntegerProperty selectedAnalysisIdProperty() {
		return selectedAnalysisId;
	}
	
	public void setSelectedProteinGroupId(int id) {
		this.selectedProteinGroupId.set(id);
	}
	
	public int getSelectedProteinGroupId() {
		return selectedProteinGroupId.get();
	}
	
	public IntegerProperty selectedProteinGroupIdProperty() {
		return selectedProteinGroupId;
	}
	
	public void setSelectedPeptideId(int id) {
		this.selectedPeptideId.set(id);
	}
	
	public int getSelectedPeptideId() {
		return selectedPeptideId.get();
	}
	
	public IntegerProperty selectedPeptideIdProperty() {
		return selectedPeptideId;
	}
	
	private int getNextId(Set<Integer> keys) {
		if (keys == null) {
			return 0;
		}
		if (keys.isEmpty()) {
			return 0;
		}
		Integer prevKey = -1;
		for (Integer key : keys) {
			if (key - prevKey != 1) {
				return key - 1;
			}
			prevKey = key;
		}
		return prevKey + 1;
 	}
}