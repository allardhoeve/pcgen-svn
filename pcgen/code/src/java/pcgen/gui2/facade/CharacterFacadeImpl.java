/*
 * CharacterFacadeImpl.java
 * Copyright 2009 (C) James Dempsey
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 12/05/2009 6:43:46 PM
 *
 * $Id$
 */
package pcgen.gui2.facade;

import gmgen.pluginmgr.GMBus;
import gmgen.pluginmgr.messages.PCClosedMessage;

import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.undo.UndoManager;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import pcgen.cdom.base.AssociatedPrereqObject;
import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.Constants;
import pcgen.cdom.content.Selection;
import pcgen.cdom.enumeration.BiographyField;
import pcgen.cdom.enumeration.EquipmentLocation;
import pcgen.cdom.enumeration.Gender;
import pcgen.cdom.enumeration.Handed;
import pcgen.cdom.enumeration.IntegerKey;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.enumeration.Nature;
import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.SkillCost;
import pcgen.cdom.enumeration.SkillFilter;
import pcgen.cdom.enumeration.StringKey;
import pcgen.cdom.enumeration.Type;
import pcgen.cdom.facet.FacetLibrary;
import pcgen.cdom.facet.event.DataFacetChangeEvent;
import pcgen.cdom.facet.event.DataFacetChangeListener;
import pcgen.cdom.facet.model.LanguageFacet;
import pcgen.cdom.facet.model.TemplateSelectionFacet;
import pcgen.cdom.helper.ClassSource;
import pcgen.cdom.inst.PCClassLevel;
import pcgen.cdom.reference.CDOMDirectSingleRef;
import pcgen.cdom.reference.CDOMSingleRef;
import pcgen.core.Ability;
import pcgen.core.AbilityCategory;
import pcgen.core.AgeSet;
import pcgen.core.BonusManager;
import pcgen.core.BonusManager.TempBonusInfo;
import pcgen.core.Deity;
import pcgen.core.Domain;
import pcgen.core.Equipment;
import pcgen.core.EquipmentModifier;
import pcgen.core.GameMode;
import pcgen.core.GearBuySellScheme;
import pcgen.core.Globals;
import pcgen.core.Kit;
import pcgen.core.Language;
import pcgen.core.PCAlignment;
import pcgen.core.PCClass;
import pcgen.core.PCStat;
import pcgen.core.PCTemplate;
import pcgen.core.PObject;
import pcgen.core.PlayerCharacter;
import pcgen.core.QualifiedObject;
import pcgen.core.Race;
import pcgen.core.RollingMethods;
import pcgen.core.RuleConstants;
import pcgen.core.SettingsHandler;
import pcgen.core.SimpleFacadeImpl;
import pcgen.core.SizeAdjustment;
import pcgen.core.Skill;
import pcgen.core.VariableProcessor;
import pcgen.core.analysis.DomainApplication;
import pcgen.core.analysis.SkillRankControl;
import pcgen.core.analysis.SpellCountCalc;
import pcgen.core.bonus.BonusObj;
import pcgen.core.character.CharacterSpell;
import pcgen.core.character.EquipSet;
import pcgen.core.character.Follower;
import pcgen.core.chooser.ChoiceManagerList;
import pcgen.core.chooser.ChooserUtilities;
import pcgen.core.display.CharacterDisplay;
import pcgen.core.facade.AbilityCategoryFacade;
import pcgen.core.facade.AbilityFacade;
import pcgen.core.facade.AlignmentFacade;
import pcgen.core.facade.CampaignFacade;
import pcgen.core.facade.CharacterFacade;
import pcgen.core.facade.CharacterLevelFacade;
import pcgen.core.facade.CharacterLevelsFacade;
import pcgen.core.facade.CharacterLevelsFacade.CharacterLevelEvent;
import pcgen.core.facade.CharacterLevelsFacade.HitPointListener;
import pcgen.core.facade.CharacterStubFacade;
import pcgen.core.facade.ClassFacade;
import pcgen.core.facade.CompanionSupportFacade;
import pcgen.core.facade.DataSetFacade;
import pcgen.core.facade.DefaultReferenceFacade;
import pcgen.core.facade.DeityFacade;
import pcgen.core.facade.DescriptionFacade;
import pcgen.core.facade.DomainFacade;
import pcgen.core.facade.EquipModFacade;
import pcgen.core.facade.EquipmentFacade;
import pcgen.core.facade.EquipmentListFacade;
import pcgen.core.facade.EquipmentListFacade.EquipmentListEvent;
import pcgen.core.facade.EquipmentListFacade.EquipmentListListener;
import pcgen.core.facade.EquipmentSetFacade;
import pcgen.core.facade.GearBuySellFacade;
import pcgen.core.facade.GenderFacade;
import pcgen.core.facade.HandedFacade;
import pcgen.core.facade.InfoFacade;
import pcgen.core.facade.InfoFactory;
import pcgen.core.facade.KitFacade;
import pcgen.core.facade.LanguageChooserFacade;
import pcgen.core.facade.LanguageFacade;
import pcgen.core.facade.RaceFacade;
import pcgen.core.facade.ReferenceFacade;
import pcgen.core.facade.SimpleFacade;
import pcgen.core.facade.SkillFacade;
import pcgen.core.facade.SpellFacade;
import pcgen.core.facade.SpellSupportFacade;
import pcgen.core.facade.StatFacade;
import pcgen.core.facade.TempBonusFacade;
import pcgen.core.facade.TemplateFacade;
import pcgen.core.facade.TodoFacade;
import pcgen.core.facade.UIDelegate;
import pcgen.core.facade.UIDelegate.CustomEquipResult;
import pcgen.core.facade.event.ChangeListener;
import pcgen.core.facade.event.ListEvent;
import pcgen.core.facade.event.ListListener;
import pcgen.core.facade.util.DefaultListFacade;
import pcgen.core.facade.util.ListFacade;
import pcgen.core.facade.util.ListFacades;
import pcgen.core.kit.BaseKit;
import pcgen.core.pclevelinfo.PCLevelInfo;
import pcgen.core.prereq.PrereqHandler;
import pcgen.core.spell.Spell;
import pcgen.core.utils.CoreUtility;
import pcgen.core.utils.MessageType;
import pcgen.core.utils.ShowMessageDelegate;
import pcgen.gui2.UIPropertyContext;
import pcgen.gui2.util.HtmlInfoBuilder;
import pcgen.io.ExportHandler;
import pcgen.io.PCGIOHandler;
import pcgen.system.CharacterManager;
import pcgen.system.LanguageBundle;
import pcgen.system.PCGenSettings;
import pcgen.util.Logging;
import pcgen.util.enumeration.Load;
import pcgen.util.enumeration.Tab;

/**
 * The Class <code>CharacterFacadeImpl</code> is an implementation of 
 * the {@link CharacterFacade} interface for the new user interface. It is 
 * intended to provide a full implementation of the new ui/core 
 * interaction layer.
 * <p>
 * <b>Issues needing resolution:</b>
 * <ul>
 * <li>Who is responsible for undo management and how will it work?</li>
 * </ul>
 * <br/>
 * Last Editor: $Author$ <br/>
 * Last Edited: $Date$
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision$
 */
public class CharacterFacadeImpl implements CharacterFacade, EquipmentListListener, ListListener<EquipmentFacade>,
		HitPointListener 
{

	private List<ClassFacade> pcClasses;
	private DefaultListFacade<TempBonusFacade> appliedTempBonuses;
	private DefaultListFacade<TempBonusFacade> availTempBonuses;
	private DefaultReferenceFacade<AlignmentFacade> alignment;
	private DefaultListFacade<EquipmentSetFacade> equipmentSets;
	private DefaultReferenceFacade<GenderFacade> gender;
	private DefaultListFacade<CharacterLevelFacade> pcClassLevels;
	private Map<StatFacade, DefaultReferenceFacade<Integer>> statScoreMap;
	private UndoManager undoManager;
	private DataSetFacade dataSet;
	private DefaultReferenceFacade<RaceFacade> race;
	private DefaultReferenceFacade<DeityFacade> deity;
	private DefaultReferenceFacade<String> tabName;
	private DefaultReferenceFacade<String> name;
	private DefaultReferenceFacade<String> playersName;
	private final PlayerCharacter theCharacter;
	private final CharacterDisplay charDisplay;
	private DefaultReferenceFacade<EquipmentSetFacade> equipSet;
	private DefaultListFacade<LanguageFacade> languages;
	private EquipmentListFacadeImpl purchasedEquip;
	private DefaultReferenceFacade<File> file;
	private DefaultReferenceFacade<HandedFacade> handedness;
	private UIDelegate delegate;
	private List<Language> autoLanguagesCache;
	private CharacterLevelsFacadeImpl charLevelsFacade;
	private DefaultReferenceFacade<Integer> currentXP;
	private DefaultReferenceFacade<Integer> xpForNextlevel;
	private DefaultReferenceFacade<String> xpTableName;
	private DefaultReferenceFacade<String> characterType;
	private DefaultReferenceFacade<String> previewSheet;
	private DefaultReferenceFacade<SkillFilter> skillFilter;
	private DefaultReferenceFacade<Integer> age;
	private DefaultReferenceFacade<SimpleFacade> ageCategory;
	private DefaultListFacade<SimpleFacade> ageCategoryList;
	private DefaultReferenceFacade<String> poolPointText;
	private DefaultReferenceFacade<String> statTotalLabelText;
	private DefaultReferenceFacade<String> statTotalText;
	private DefaultReferenceFacade<String> modTotalLabelText;
	private DefaultReferenceFacade<String> modTotalText;
	private DefaultReferenceFacade<Integer> numBonusLang;
	private DefaultReferenceFacade<Integer> numSkillLang;
	private DefaultReferenceFacade<Integer> hpRef;
	private DefaultReferenceFacade<Integer> rollMethodRef;
	private DefaultReferenceFacade<String> carriedWeightRef;
	private DefaultReferenceFacade<String> loadRef;
	private DefaultReferenceFacade<String> weightLimitRef;
	private DefaultListFacade<DomainFacade> domains;
	private DefaultListFacade<DomainFacade> availDomains;
	private DefaultReferenceFacade<Integer> maxDomains;
	private DefaultReferenceFacade<Integer> remainingDomains;
	private DefaultListFacade<TemplateFacade> templates;
	private DefaultListFacade<RaceFacade> raceList;
	private DefaultListFacade<KitFacade> kitList;
	private DefaultReferenceFacade<File> portrait;
	private RectangleReference cropRect;
	private String selectedGender;
	private List<Language> currBonusLangs;
	private DefaultReferenceFacade<String> skinColor;
	private DefaultReferenceFacade<String> hairColor;
	private DefaultReferenceFacade<String> eyeColor;
	private DefaultReferenceFacade<Integer> heightRef;
	private DefaultReferenceFacade<Integer> weightRef;
	private DefaultReferenceFacade<BigDecimal> fundsRef;
	private DefaultReferenceFacade<BigDecimal> wealthRef;
	private DefaultReferenceFacade<GearBuySellFacade> gearBuySellSchemeRef;

	private Gui2InfoFactory infoFactory;
	private CharacterAbilities characterAbilities;
	private DescriptionFacade descriptionFacade;
	private SpellSupportFacadeImpl spellSupportFacade;
	private CompanionSupportFacadeImpl companionSupportFacade;
	private TodoManager todoManager;
	private boolean allowDebt;

	private int lastExportCharSerial = 0;
	private PlayerCharacter lastExportChar = null;
	private LanguageListener langListener;
	private TemplateListener templateListener;
	
	/**
	 * Create a new character facade for an existing character.
	 * 
	 * @param pc The character to be represented
	 * @param delegate the UIDelegate for this CharacterFacade
	 * @param dataSetFacade The data set in use for the character
	 */
	public CharacterFacadeImpl(PlayerCharacter pc, UIDelegate delegate, DataSetFacade dataSetFacade) 
	{
		this.delegate = delegate;
		theCharacter = pc;
		charDisplay = pc.getDisplay();
		dataSet = dataSetFacade;
		buildAgeCategories();
		initForCharacter();
		undoManager = new UndoManager();
	}

	/**
	 * {@inheritDoc} 
	 */
    @Override
	public void closeCharacter()
	{
		FacetLibrary.getFacet(LanguageFacet.class)
			.removeDataFacetChangeListener(langListener);
		FacetLibrary.getFacet(TemplateSelectionFacet.class)
			.removeDataFacetChangeListener(templateListener);
		characterAbilities.closeCharacter();
		charLevelsFacade.closeCharacter();
		GMBus.send(new PCClosedMessage(null, theCharacter));
		Globals.getPCList().remove(theCharacter);
	}
	
	/**
	 * 
	 */
	private void initForCharacter()
	{
		// Calculate any active bonuses
		theCharacter.preparePCForOutput();

		todoManager = new TodoManager();

		infoFactory = new Gui2InfoFactory(theCharacter);
		characterAbilities = new CharacterAbilities(theCharacter, delegate, dataSet, todoManager);
		descriptionFacade = new DescriptionFacadeImpl(theCharacter);
		spellSupportFacade = new SpellSupportFacadeImpl(theCharacter, delegate, dataSet, todoManager, this);

		name = new DefaultReferenceFacade<String>(charDisplay.getName());
		file = new DefaultReferenceFacade<File>(new File(charDisplay.getFileName()));
		
		companionSupportFacade = new CompanionSupportFacadeImpl(theCharacter, todoManager, name, file, this);
		
		availTempBonuses = new DefaultListFacade<TempBonusFacade>();
		refreshAvailableTempBonuses();
		appliedTempBonuses = new DefaultListFacade<TempBonusFacade>();
		buildAppliedTempBonusList();
		kitList = new DefaultListFacade<KitFacade>();
		refreshKitList();

		statScoreMap = new HashMap<StatFacade, DefaultReferenceFacade<Integer>>();

		File portraitFile = null;
		if (!StringUtils.isEmpty(charDisplay.getPortraitPath()))
		{
			portraitFile = new File(charDisplay.getPortraitPath());
		}
		portrait = new DefaultReferenceFacade<File>(portraitFile);
		cropRect = new RectangleReference(charDisplay.getPortraitThumbnailRect());
		characterType = new DefaultReferenceFacade<String>(charDisplay.getCharacterType());
		previewSheet = new DefaultReferenceFacade<String>(charDisplay.getPreviewSheet());
		skillFilter = new DefaultReferenceFacade<SkillFilter>(charDisplay.getSkillFilter());

		tabName = new DefaultReferenceFacade<String>(charDisplay.getTabName());
		playersName = new DefaultReferenceFacade<String>(charDisplay.getPlayersName());
		race = new DefaultReferenceFacade<RaceFacade>(charDisplay.getRace());
		raceList = new DefaultListFacade<RaceFacade>();
		if (charDisplay.getRace() != null && charDisplay.getRace() != Globals.s_EMPTYRACE)
		{
			raceList.addElement(charDisplay.getRace());
		}
		handedness = new DefaultReferenceFacade<HandedFacade>();
		gender = new DefaultReferenceFacade<GenderFacade>();
		if (charDisplay.getRace() != null)
		{
			for (HandedFacade handsFacade : charDisplay.getRace().getHands())
			{
				if (handsFacade.equals(charDisplay.getHandedObject()))
				{
					handedness.setReference(handsFacade);
					break;
				}
			}
			for (GenderFacade pcGender : race.getReference().getGenders())
			{
				if (pcGender.equals(charDisplay.getGenderObject()))
				{
					gender.setReference(pcGender);
					break;
				}
			}
		}

		alignment = new DefaultReferenceFacade<AlignmentFacade>(charDisplay.getPCAlignment());
		age = new DefaultReferenceFacade<Integer>(charDisplay.getAge());
		ageCategory = new DefaultReferenceFacade<SimpleFacade>();
		updateAgeCategoryForAge();
		currentXP = new DefaultReferenceFacade<Integer>(charDisplay.getXP());
		xpForNextlevel = new DefaultReferenceFacade<Integer>(charDisplay.minXPForNextECL());
		xpTableName = new DefaultReferenceFacade<String>(charDisplay.getXPTableName());
		hpRef = new DefaultReferenceFacade<Integer>(theCharacter.hitPoints());

		skinColor = new DefaultReferenceFacade<String>(charDisplay.getSkinColor());
		hairColor = new DefaultReferenceFacade<String>(charDisplay.getHairColor());
		eyeColor = new DefaultReferenceFacade<String>(charDisplay.getEyeColor());
		weightRef = new DefaultReferenceFacade<Integer>();
		heightRef = new DefaultReferenceFacade<Integer>();
		refreshHeightWeight();

		purchasedEquip = new EquipmentListFacadeImpl(theCharacter.getEquipmentMasterList());
		carriedWeightRef = new DefaultReferenceFacade<String>();
		loadRef = new DefaultReferenceFacade<String>();
		weightLimitRef = new DefaultReferenceFacade<String>();
		equipSet = new DefaultReferenceFacade<EquipmentSetFacade>();
		equipmentSets = new DefaultListFacade<EquipmentSetFacade>();
		initEquipSet();

		GameMode game = (GameMode) dataSet.getGameMode();
		rollMethodRef = new DefaultReferenceFacade<Integer>(game.getRollMethod());

		charLevelsFacade =
				new CharacterLevelsFacadeImpl(theCharacter, delegate,
					todoManager, dataSet, this);
		pcClasses = new ArrayList<ClassFacade>();
		pcClassLevels = new DefaultListFacade<CharacterLevelFacade>();
		refreshClassLevelModel();
		charLevelsFacade.addHitPointListener(this);

		deity =
				new DefaultReferenceFacade<DeityFacade>(charDisplay.getDeity());
		domains = new DefaultListFacade<DomainFacade>();
		maxDomains =
				new DefaultReferenceFacade<Integer>(
					theCharacter.getMaxCharacterDomains());
		remainingDomains =
				new DefaultReferenceFacade<Integer>(
					theCharacter.getMaxCharacterDomains() - domains.getSize());
		availDomains = new DefaultListFacade<DomainFacade>();
		buildAvailableDomainsList();

		templates =
				new DefaultListFacade<TemplateFacade>(
					charDisplay.getDisplayVisibleTemplateList());
		templateListener = new TemplateListener(); 
		FacetLibrary.getFacet(TemplateSelectionFacet.class).addDataFacetChangeListener(templateListener);

		initTodoList();

		statTotalLabelText = new DefaultReferenceFacade<String>();
		statTotalText = new DefaultReferenceFacade<String>();
		modTotalLabelText = new DefaultReferenceFacade<String>();
		modTotalText = new DefaultReferenceFacade<String>();
		updateScorePurchasePool(false);

		languages = new DefaultListFacade<LanguageFacade>();
		numBonusLang = new DefaultReferenceFacade<Integer>(0);
		numSkillLang = new DefaultReferenceFacade<Integer>(0);
		refreshLanguageList();
		langListener = new LanguageListener();
		FacetLibrary.getFacet(LanguageFacet.class).addDataFacetChangeListener(langListener);

		purchasedEquip.addListListener(spellSupportFacade);
		purchasedEquip.addEquipmentListListener(spellSupportFacade);
		fundsRef = new DefaultReferenceFacade<BigDecimal>(theCharacter.getGold());
		wealthRef = new DefaultReferenceFacade<BigDecimal>(theCharacter.totalValue());
		gearBuySellSchemeRef = new DefaultReferenceFacade<GearBuySellFacade>(findGearBuySellRate());
		allowDebt = false;
	}

	/**
	 * Build up the list of kits that the character has.
	 */
	private void refreshKitList()
	{
		List<Kit> kits = new ArrayList<Kit>();
		for (Kit kit : charDisplay.getKitInfo())
		{
			kits.add(kit);
		}
		kitList.updateContents(kits);
	}

	private GearBuySellFacade findGearBuySellRate()
	{
		int buyRate = SettingsHandler.getGearTab_BuyRate();
		int sellRate = SettingsHandler.getGearTab_SellRate();
		for (GearBuySellFacade buySell : dataSet.getGearBuySellSchemes())
		{
			GearBuySellScheme scheme = (GearBuySellScheme) buySell;
			if (scheme.getBuyRate().intValue() == buyRate && scheme.getSellRate().intValue() == sellRate)
			{
				return scheme;
			}
		}

		GearBuySellScheme scheme =
				new GearBuySellScheme(LanguageBundle.getString("in_custom"), //$NON-NLS-1$
					new BigDecimal(buyRate), new BigDecimal(sellRate),
					new BigDecimal(100));
		return scheme;
	}

	/**
	 * Initialise the equipment set facades, ensuring that the character has a 
	 * default equipment set. 
	 * @param pc The character being loaded
	 */
	private void initEquipSet()
	{
		// Setup the default EquipSet if not already present
		if (!charDisplay.hasEquipSet())
		{
			String id = EquipmentSetFacadeImpl.getNewIdPath(charDisplay, null);
			EquipSet eSet = new EquipSet(id, LanguageBundle.getString("in_ieDefault"));
			theCharacter.addEquipSet(eSet);
			theCharacter.setCalcEquipSetId(id);
		}

		// Detach listeners from old set
		if (equipSet.getReference() != null)
		{
			EquipmentListFacade equippedItems = equipSet.getReference().getEquippedItems();
			equippedItems.removeListListener(this);
			equippedItems.removeEquipmentListListener(this);
		}
		
		// Make facades for each root equipset.
		List<EquipmentSetFacade> eqSetList = new ArrayList<EquipmentSetFacade>();
		EquipmentSetFacade currSet = null;
		String currIdPath = theCharacter.getCalcEquipSetId();
		for (EquipSet es : charDisplay.getEquipSet())
		{
			if (es.getParentIdPath().equals("0"))
			{
				final EquipmentSetFacadeImpl facade =
						new EquipmentSetFacadeImpl(delegate, theCharacter, es,
							dataSet, purchasedEquip, todoManager, this);
				eqSetList.add(facade);
				if (es.getIdPath().equals(currIdPath))
				{
					currSet = facade;
				}
			}
		}
		equipmentSets.updateContents(eqSetList);
		if (currSet != null)
		{
			equipSet.setReference(currSet);
		}

		EquipmentSetFacade set = equipSet.getReference();
		set.getEquippedItems().addListListener(this);
		set.getEquippedItems().addEquipmentListListener(this);
		refreshTotalWeight();

	}

	/**
	 * Create the list of known age categories in the current BioSet. 
	 */
	private void buildAgeCategories()
	{
		List<String> cats = new ArrayList<String>();
		for (String aString : Globals.getBioSet().getAgeCategories())
		{
			final int idx = aString.indexOf('\t');

			if (idx >= 0)
			{
				aString = aString.substring(0, idx);
			}

			if (!cats.contains(aString))
			{
				cats.add(aString);
			}
		}
		Collections.sort(cats);
		ageCategoryList = new DefaultListFacade<SimpleFacade>();
		for (String ageCat : cats)
		{
			ageCategoryList.addElement(new SimpleFacadeImpl(ageCat));
		}
	}

	/**
	 * Create an initial list of todo items 
	 */
	private void initTodoList()
	{
		if (isNewCharName(charDisplay.getName()))
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY, "Name", "in_sumTodoName", 1));
		}
		if (charDisplay.getRace() == null || Constants.NONESELECTED.equals(charDisplay.getRace().getKeyName()))
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY, "Race", "in_irTodoRace", 100));
		}

		// Stats todo already done in updateScorePurchasePool
		updateLevelTodo();
	}

	/**
	 * Identify if the supplied name is a default one generated by the system
	 * e.g. Unnamed 1 or Unnamed 2
	 * @param charName The name to be checked.
	 * @return True if the name is a default.
	 */
	private boolean isNewCharName(String charName)
	{
		if (charName == null)
		{
			return true;
		}

		return charName.startsWith("Unnamed"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#addAbility(pcgen.core.facade.AbilityCategoryFacade, pcgen.core.facade.AbilityFacade)
	 */
	@Override
	public void addAbility(AbilityCategoryFacade category, AbilityFacade ability)
	{
		characterAbilities.addAbility(category, ability);
		refreshKitList();
		refreshAvailableTempBonuses();
		buildAvailableDomainsList();
		companionSupportFacade.refreshCompanionData();
		refreshEquipment();
		hpRef.setReference(theCharacter.hitPoints());
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#removeAbility(pcgen.core.facade.AbilityCategoryFacade, pcgen.core.facade.AbilityFacade)
	 */
	@Override
	public void removeAbility(AbilityCategoryFacade category, AbilityFacade ability)
	{
		characterAbilities.removeAbility(category, ability);
		refreshKitList();
		companionSupportFacade.refreshCompanionData();
		hpRef.setReference(theCharacter.hitPoints());
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAbilities(pcgen.core.facade.AbilityCategoryFacade)
	 */
	@Override
	public ListFacade<AbilityFacade> getAbilities(AbilityCategoryFacade category)
	{
		return characterAbilities.getAbilities(category);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getActiveAbilityCategories()
	 */
	@Override
	public ListFacade<AbilityCategoryFacade> getActiveAbilityCategories()
	{
		return characterAbilities.getActiveAbilityCategories();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getTotalSelections(pcgen.core.facade.AbilityCategoryFacade)
	 */
	@Override
	public int getTotalSelections(AbilityCategoryFacade category)
	{
		return characterAbilities.getTotalSelections(category);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getRemainingSelections(pcgen.core.facade.AbilityCategoryFacade)
	 */
	@Override
	public int getRemainingSelections(AbilityCategoryFacade category)
	{
		return characterAbilities.getRemainingSelections(category);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAbilityCatSelectionListener(ChangeListener listener)
	{
		characterAbilities.addAbilityCatSelectionListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAbilityCatSelectionListener(ChangeListener listener)
	{
		characterAbilities.removeAbilityCatSelectionListener(listener);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setRemainingSelection(pcgen.core.facade.AbilityCategoryFacade, int)
	 */
	@Override
	public void setRemainingSelection(AbilityCategoryFacade category, int remaining)
	{
		characterAbilities.setRemainingSelection(category, remaining);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#hasAbility(pcgen.core.facade.AbilityCategoryFacade, pcgen.core.facade.AbilityFacade)
	 */
	@Override
	public boolean hasAbility(AbilityCategoryFacade category, AbilityFacade ability)
	{
		return characterAbilities.hasAbility(category, ability);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAbilityNature(pcgen.core.facade.AbilityFacade)
	 */
	@Override
	public Nature getAbilityNature(AbilityFacade ability)
	{
		if (ability == null || !(ability instanceof Ability))
		{
			return null;
		}
		return theCharacter.getAbilityNature((Ability) ability);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#addCharacterLevels(pcgen.core.facade.ClassFacade[])
	 */
	@Override
	public void addCharacterLevels(ClassFacade[] classes)
	{
		SettingsHandler.setShowHPDialogAtLevelUp(false);
		//SettingsHandler.setShowStatDialogAtLevelUp(false);

		int oldLevel = charLevelsFacade.getSize();
		boolean needFullRefresh = false;

		for (ClassFacade classFacade : classes)
		{
			if (classFacade instanceof PCClass)
			{
				int totalLevels = charDisplay.getTotalLevels();
				if (!validateAddLevel((PCClass) classFacade))
				{
					return;
				}
				Logging.log(Logging.INFO, charDisplay.getName()
					+ ": Adding level " + (totalLevels + 1) //$NON-NLS-1$
					+ " in class " + classFacade); //$NON-NLS-1$
				theCharacter.incrementClassLevel(1, (PCClass) classFacade);
				if (totalLevels == charDisplay.getTotalLevels())
				{
					// The level change was rejected - no further processing needed.
					return;
				}
				if (((PCClass) classFacade).containsKey(ObjectKey.EXCHANGE_LEVEL))
				{
					needFullRefresh = true;
				}
			}
			if (!pcClasses.contains(classFacade))
			{
				pcClasses.add(classFacade);
			}
			CharacterLevelFacadeImpl cl = new CharacterLevelFacadeImpl(classFacade,
					charLevelsFacade.getSize() + 1);
			pcClassLevels.addElement(cl);
			charLevelsFacade.addLevelOfClass(cl);
		}
		CharacterUtils.selectClothes(getTheCharacter());
		
		// Calculate any active bonuses
		theCharacter.calcActiveBonuses();

		if (needFullRefresh)
		{
			refreshClassLevelModel();
		}
		postLevellingUpdates();
		delegate.showLevelUpInfo(this, oldLevel);
	}

	/**
	 * Ensure any items that could be affected by the level up or down are refreshed.
	 */
	void postLevellingUpdates()
	{
		characterAbilities.rebuildAbilityLists();
		companionSupportFacade.refreshCompanionData();
		refreshKitList();
		refreshAvailableTempBonuses();
		refreshEquipment();
		currentXP.setReference(charDisplay.getXP());
		xpForNextlevel.setReference(charDisplay.minXPForNextECL());
		xpTableName.setReference(charDisplay.getXPTableName());
		hpRef.setReference(theCharacter.hitPoints());
		age.setReference(charDisplay.getAge());
		refreshHeightWeight();
		refreshStatScores();

		updateLevelTodo();
		buildAvailableDomainsList();
		spellSupportFacade.refreshAvailableKnownSpells();
		updateScorePurchasePool(false);
		refreshLanguageList();
	}

	/**
	 * Ensure any items that could be affected by the new equipment are refreshed.
	 */
	void postEquippingUpdates()
	{
		characterAbilities.rebuildAbilityLists();
		refreshAvailableTempBonuses();
		hpRef.setReference(theCharacter.hitPoints());
	}

	private void refreshHeightWeight()
	{
		weightRef.setReference(Globals.getGameModeUnitSet()
			.convertWeightToUnitSet(charDisplay.getWeight()));
		heightRef.setReference((int) Math.round(Globals.getGameModeUnitSet()
			.convertHeightToUnitSet(charDisplay.getHeight())));
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#removeCharacterLevels(int)
	 */
	@Override
	public void removeCharacterLevels(int levels)
	{
		for (int i = levels; i > 0 && !pcClassLevels.isEmpty(); i--)
		{
			ClassFacade classFacade = charLevelsFacade
					.getClassTaken(pcClassLevels.getElementAt(pcClassLevels.getSize() - 1));
			pcClassLevels.removeElement(pcClassLevels.getSize() - 1);
			if (classFacade instanceof PCClass)
			{
				Logging.log(Logging.INFO, charDisplay.getName()
					+ ": Removing level " + (pcClassLevels.getSize()+1) //$NON-NLS-1$
					+ " in class " + classFacade); //$NON-NLS-1$
				theCharacter.incrementClassLevel(-1, (PCClass) classFacade);
			}
			charLevelsFacade.removeLastLevel();
		}

		// Clean up the class list 
		for (Iterator<ClassFacade> iterator = pcClasses.iterator(); iterator.hasNext();)
		{
			ClassFacade classFacade = iterator.next();
			boolean stillPresent = false;
			for (CharacterLevelFacade charLevel : pcClassLevels)
			{
				if (charLevelsFacade.getClassTaken(charLevel) == classFacade)
				{
					stillPresent = true;
					break;
				}
			}

			if (!stillPresent)
			{
				iterator.remove();
			}
		}
		postLevellingUpdates();
	}

	/**
	 * Update the todo list to reflect the change in level or experience.
	 */
	private void updateLevelTodo()
	{
		if (charDisplay.getXP() >= charDisplay.minXPForNextECL())
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY, "Class", "in_clTodoLevelUp", 120));
		} else
		{
			todoManager.removeTodo("in_clTodoLevelUp");
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getClassLevel(pcgen.core.facade.ClassFacade)
	 */
	@Override
	public int getClassLevel(ClassFacade c)
	{
		int clsLevel = 0;
		// We have to compare by class key as classes get cloned and we may have multiple instances of the same class in our level list 
		String classKey = c.getKeyName();
		for (CharacterLevelFacade charLevel : pcClassLevels)
		{
			if (charLevelsFacade.getClassTaken(charLevel).getKeyName().equals(classKey))
			{
				clsLevel++;
			}
		}
		return clsLevel;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getLevels()
	 */
	@Override
	public ListFacade<CharacterLevelFacade> getLevels()
	{
		return pcClassLevels;
	}

	private boolean validateAddLevel(PCClass theClass)
	{
		int levels = 1;

		if (theClass == null)
		{
			return false;
		}

		if (!theCharacter.isQualified(theClass))
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME,
					LanguageBundle.getString("in_clYouAreNotQualifiedToTakeTheClass"));
			return false;
		}

		if (!theCharacter.canLevelUp())
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME, LanguageBundle.getString("in_Enforce_rejectLevelUp"));
			return false;
		}

		final PCClass aClass = theCharacter.getClassKeyed(theClass.getKeyName());

		// Check if the subclass (if any) is qualified for
		if (aClass != null)
		{
			String subClassKey = charDisplay.getSubClassName(aClass);
			if (subClassKey != null)
			{
				final PCClass subClass = aClass.getSubClassKeyed(subClassKey);
				if (subClass != null && !theCharacter.isQualified(subClass))
				{
					delegate.showErrorMessage(Constants.APPLICATION_NAME,
						LanguageBundle.getFormattedString(
							"in_sumYouAreNotQualifiedToTakeTheClass",//$NON-NLS-1$
							aClass.getDisplayName()
								+ "/" + subClass.getDisplayName()));//$NON-NLS-1$
					return false;
				}
			}
		}

		if (!Globals.checkRule(RuleConstants.LEVELCAP)
				&& theClass.hasMaxLevel()
				&& ((levels > theClass.getSafe(IntegerKey.LEVEL_LIMIT)) || ((aClass != null) && ((charDisplay
						.getLevel(aClass) + levels) > aClass.getSafe(IntegerKey.LEVEL_LIMIT)))))
		{
			delegate.showInfoMessage(Constants.APPLICATION_NAME,
					LanguageBundle.getFormattedString("in_sumMaximumLevelIs", //$NON-NLS-1$
							String.valueOf(theClass.getSafe(IntegerKey.LEVEL_LIMIT))));
			return false;
		}

		// Check with the user on their first level up
		if (charDisplay.getTotalLevels() == 0)
		{
			if (SettingsHandler.getGame().isPurchaseStatMode()
					&& (theCharacter.getPointBuyPoints() > getUsedStatPool()))
			{
				if (!delegate.showWarningConfirm(LanguageBundle.getString("in_sumLevelWarnTitle"),//$NON-NLS-1$
						LanguageBundle.getString("in_sumPoolWarning")))//$NON-NLS-1$
				{
					return false;
				}
			} else if (allAbilitiesAreZero())
			{
				if (!delegate.showWarningConfirm(LanguageBundle.getString("in_sumLevelWarnTitle"),
						LanguageBundle.getString("in_sumAbilitiesZeroWarning")))
				{
					return false;
				}
			} else
			{
				Boolean proceed = delegate.maybeShowWarningConfirm(LanguageBundle.getString("in_sumLevelWarnTitle"),
						LanguageBundle.getString("in_sumAbilitiesWarning"),
						LanguageBundle.getString("in_sumAbilitiesWarningCheckBox"), PCGenSettings.OPTIONS_CONTEXT,
						PCGenSettings.OPTION_SHOW_WARNING_AT_FIRST_LEVEL_UP);
				if (Boolean.FALSE.equals(proceed))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Determine if all of the character's stats are still set to 0.
	 *
	 * @return True if they are all zero, false if any are non-zero.
	 */
	private boolean allAbilitiesAreZero()
	{
		for (StatFacade stat : dataSet.getStats())
		{
			ReferenceFacade<Integer> facade = getScoreBaseRef(stat);

			if (facade.getReference() != 0)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * This method gets the number of stat points used in the pool
	 * @param pc The PlayerCharacter to get used stat pool for
	 * @return used stat pool
	 */
	private int getUsedStatPool()
	{
		int i = 0;

		for (PCStat aStat : charDisplay.getStatSet())
		{
			if (!aStat.getSafe(ObjectKey.ROLLED))
			{
				continue;
			}

			final int statValue = theCharacter.getBaseStatFor(aStat);
			i += getPurchaseCostForStat(theCharacter, statValue);
		}
		i += (int) theCharacter.getTotalBonusTo("POINTBUY", "SPENT"); //$NON-NLS-1$ //$NON-NLS-2$
		return i;
	}

	private static int getPurchaseCostForStat(final PlayerCharacter aPC, int statValue)
	{
		final int iMax = SettingsHandler.getGame().getPurchaseScoreMax(aPC);
		final int iMin = SettingsHandler.getGame().getPurchaseScoreMin(aPC);

		if (statValue > iMax)
		{
			statValue = iMax;
		}

		if (statValue >= iMin)
		{
			return SettingsHandler.getGame().getAbilityScoreCost(statValue - iMin);
		}
		return 0;
	}

	void refreshAvailableTempBonuses()
	{
		List<TempBonusFacadeImpl> tempBonuses = new ArrayList<TempBonusFacadeImpl>();

		//
		// first do PC's feats and other abilities
		for (Ability aFeat : theCharacter.getFullAbilitySet())
		{
			scanForTempBonuses(tempBonuses, aFeat);
		}

		//
		// next do all Feats to get TEMPBONUS:ANYPC or TEMPBONUS:EQUIP
		for (Ability aFeat : Globals.getContext().ref.getManufacturer(Ability.class, AbilityCategory.FEAT)
				.getAllObjects())
		{
			scanForNonPcTempBonuses(tempBonuses, aFeat);
		}

		//
		// Do all the PC's spells
		for (Spell aSpell : theCharacter.aggregateSpellList("", "", "", 0, 9))
		{
			scanForTempBonuses(tempBonuses, aSpell);
		}

		// Do all the pc's innate spells.
		Collection<CharacterSpell> innateSpells = theCharacter.getCharacterSpells(charDisplay.getRace(),
				Constants.INNATE_SPELL_BOOK_NAME);
		for (CharacterSpell aCharacterSpell : innateSpells)
		{
			if (aCharacterSpell == null)
			{
				continue;
			}
			scanForTempBonuses(tempBonuses, aCharacterSpell.getSpell());
		}

		//
		// Next do all spells to get TEMPBONUS:ANYPC or TEMPBONUS:EQUIP
		for (Spell spell : Globals.getSpellMap().values())
		{
			scanForNonPcTempBonuses(tempBonuses, spell);
		}

		//
		// iterate thru all PC's equipment objects
		for (Equipment aEq : charDisplay.getEquipmentSet())
		{
			scanForTempBonuses(tempBonuses, aEq);
		}

		//
		// Do we also need to Iterate Globals.getAbilityKeyIterator(Constants.ALL_CATEGORIES); ?
		// or will they be covered by getClassList()?
		//
		// iterate thru all PC's Classes
		for (PCClass aClass : charDisplay.getClassSet())
		{
			int currentLevel = charDisplay.getLevel(aClass);
			scanForTempBonuses(tempBonuses, aClass);
			for (int i = 1; i < currentLevel; i++)
			{
				PCClassLevel pcl = charDisplay.getActiveClassLevel(aClass, i);
				scanForTempBonuses(tempBonuses, pcl);
			}
		}

		//
		// Iterate through all the PC's Templates
		for (PCTemplate aTemp : charDisplay.getTemplateSet())
		{
			scanForTempBonuses(tempBonuses, aTemp);
		}

		// do all Templates to get TEMPBONUS:ANYPC or TEMPBONUS:EQUIP
		for (PCTemplate aTemp : Globals.getContext().ref.getConstructedCDOMObjects(PCTemplate.class))
		{
			scanForNonPcTempBonuses(tempBonuses, aTemp);
		}

		//
		// Iterate through all the PC's Skills
		for (Skill aSkill : charDisplay.getSkillSet())
		{
			scanForTempBonuses(tempBonuses, aSkill);
		}

		Collections.sort(tempBonuses);
		availTempBonuses.updateContents(tempBonuses);
	}

	private void scanForNonPcTempBonuses(List<TempBonusFacadeImpl> tempBonuses, PObject obj)
	{
		if (obj == null)
		{
			return;
		}
		if (TempBonusHelper.hasNonPCTempBonus(obj, theCharacter))
		{
			tempBonuses.add(new TempBonusFacadeImpl(obj));
		}
	}

	private void scanForTempBonuses(List<TempBonusFacadeImpl> tempBonuses, CDOMObject obj)
	{
		if (obj == null)
		{
			return;
		}
		if (TempBonusHelper.hasTempBonus(obj, theCharacter))
		{
			tempBonuses.add(new TempBonusFacadeImpl(obj));
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAvailableTempBonuses()
	 */
	@Override
	public ListFacade<TempBonusFacade> getAvailableTempBonuses()
	{
		return availTempBonuses;
	}

	/**
	 * Build up the list of temporary bonuses which have been applied to this character.
	 */
	private void buildAppliedTempBonusList()
	{
		Set<String> found = new HashSet<String>();
		BonusManager bonusMgr = new BonusManager(theCharacter);
		for (Map.Entry<BonusObj, BonusManager.TempBonusInfo> me : theCharacter
				.getTempBonusMap().entrySet())
		{
			BonusObj aBonus = me.getKey();
			TempBonusInfo tbi = me.getValue();
			Object aC = tbi.source;
			Object aT = tbi.target;
			String name = bonusMgr.getBonusDisplayName(aBonus, tbi);

			if (!found.contains(name))
			{
				found.add(name);
				TempBonusFacadeImpl facade = new TempBonusFacadeImpl((CDOMObject) aC, aT, name);
				facade.setActive(!theCharacter.getTempBonusFilters().contains(
					name));
				appliedTempBonuses.addElement(facade);
			}
		}
		
		
	}

	@Override
	public void addTempBonus(TempBonusFacade bonusFacade)
	{
		if (bonusFacade == null || !(bonusFacade instanceof TempBonusFacadeImpl))
		{
			return;
		}
		TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) bonusFacade;
		
		// Allow selection of target for bonus affecting equipment
		CDOMObject originObj = tempBonus.getOriginObj();
		Equipment aEq = null;
		Object target = 
				TempBonusHelper.getTempBonusTarget(originObj,
					theCharacter, delegate, infoFactory);
		if (target == null)
		{
			return;
		}
		TempBonusFacadeImpl appliedTempBonus;
		if (target instanceof Equipment)
		{
			aEq = (Equipment) target;
			appliedTempBonus =
					TempBonusHelper.applyBonusToCharacterEquipment(aEq,
						originObj, theCharacter);
		}
		else
		{
			appliedTempBonus =
					TempBonusHelper.applyBonusToCharacter(originObj,
						theCharacter);
		}

		// Resolve choices and apply the bonus to the character.
		if (appliedTempBonus == null)
		{
			return;
		}
		
		appliedTempBonuses.addElement(appliedTempBonus);
		refreshStatScores();
		postLevellingUpdates();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTempBonus(TempBonusFacade bonusFacade)
	{
		if (bonusFacade == null || !(bonusFacade instanceof TempBonusFacadeImpl))
		{
			return;
		}
		TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) bonusFacade;
		
		Equipment aEq = null;
		if (tempBonus.getTarget() instanceof Equipment)
		{
			aEq = (Equipment) tempBonus.getTarget();
		}
		CDOMObject originObj = tempBonus.getOriginObj();
		TempBonusHelper.removeBonusFromCharacter(theCharacter, aEq, originObj);

		appliedTempBonuses.removeElement(tempBonus);
		refreshStatScores();
		postLevellingUpdates();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTempBonusActive(TempBonusFacade bonusFacade, boolean active)
	{
		if (bonusFacade == null || !(bonusFacade instanceof TempBonusFacadeImpl))
		{
			return;
		}
		TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) bonusFacade;

		if (active)
		{
			theCharacter.unsetTempBonusFilter(tempBonus.toString());
		}
		else
		{
			theCharacter.setTempBonusFilter(tempBonus.toString());
		}
		tempBonus.setActive(active);
		appliedTempBonuses.modifyElement(tempBonus);
		refreshStatScores();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListFacade<TempBonusFacade> getTempBonuses()
	{
		return appliedTempBonuses;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAlignmentRef()
	 */
	@Override
	public ReferenceFacade<AlignmentFacade> getAlignmentRef()
	{
		return alignment;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setAlignment(pcgen.core.facade.AlignmentFacade)
	 */
	@Override
	public void setAlignment(AlignmentFacade alignment)
	{
		if (!validateAlignmentChange(alignment))
		{
			return;
		}

		this.alignment.setReference(alignment);
		if (alignment instanceof PCAlignment)
		{
			theCharacter.setAlignment((PCAlignment) alignment);
		}
		refreshLanguageList();

	}

	/**
	 * Validate the new alignment matches those allowed for the character's 
	 * classes. If not offer the user a choice of backing out or making the 
	 * classes into ex-classes.
	 * 
	 * @param newAlign The alignment to be set
	 */
	private boolean validateAlignmentChange(AlignmentFacade newAlign)
	{
		AlignmentFacade oldAlign = this.alignment.getReference();

		if (oldAlign == null || newAlign.equals(oldAlign))
		{
			return true;
		}

		// We can't do any validation if the new alignment isn't a known class
		if (!(newAlign instanceof PCAlignment))
		{
			return true;
		}

		//
		// Get a list of classes that will become unqualified (and have an ex-class)
		//
		StringBuilder unqualified = new StringBuilder();
		List<PCClass> classList = charDisplay.getClassList();
		List<PCClass> exclassList = new ArrayList<PCClass>();
		PCAlignment savedAlignmnet = charDisplay.getPCAlignment();
		for (PCClass aClass : classList)
		{
			theCharacter.setAlignment((PCAlignment) newAlign);
			{
				if (!theCharacter.isQualified(aClass))
				{
					if (aClass.containsKey(ObjectKey.EX_CLASS))
					{
						if (unqualified.length() > 0)
						{
							unqualified.append(", "); //$NON-NLS-1$
						}

						unqualified.append(aClass.getKeyName());
						exclassList.add(aClass);
					}
				}
			}
		}

		//
		// Give the user a chance to bail
		//
		if (unqualified.length() > 0)
		{
			if (!delegate.showWarningConfirm(Constants.APPLICATION_NAME,
					LanguageBundle.getString("in_sumExClassesWarning") + Constants.LINE_SEPARATOR + unqualified))
			{
				theCharacter.setAlignment(savedAlignmnet);
				return false;
			}

		}

		//
		// Convert the class(es)
		//
		for (PCClass aClass : exclassList)
		{
			theCharacter.makeIntoExClass(aClass);
		}

		// Update the facade and UI
		refreshClassLevelModel();

		return true;
	}

	void refreshClassLevelModel()
	{
		List<CharacterLevelFacade> newlevels = new ArrayList<CharacterLevelFacade>();
		List<PCClass> newClasses = charDisplay.getClassList();
		Collection<PCLevelInfo> levelInfo = charDisplay.getLevelInfo();

		Map<String, PCClass> classMap = new HashMap<String, PCClass>();
		for (PCClass pcClass : newClasses)
		{
			classMap.put(pcClass.getKeyName(), pcClass);
		}

		for (PCLevelInfo lvlInfo : levelInfo)
		{
			final String classKeyName = lvlInfo.getClassKeyName();
			PCClass currClass = classMap.get(classKeyName);
			if (currClass == null)
			{
				Logging.errorPrint("No PCClass found for '" + classKeyName + "' in character's class list: "
						+ newClasses);
				return;
			}

			CharacterLevelFacadeImpl cl = new CharacterLevelFacadeImpl(currClass, newlevels.size() + 1);
			newlevels.add(cl);
		}

		pcClasses.clear();
		pcClasses.addAll(newClasses);

		pcClassLevels.updateContents(newlevels);
		// Now get the CharacterLevelsFacadeImpl to do a refresh too.
		charLevelsFacade.classListRefreshRequired();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getDataSet()
	 */
	@Override
	public DataSetFacade getDataSet()
	{
		return dataSet;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getEquipmentSets()
	 */
	@Override
	public ListFacade<EquipmentSetFacade> getEquipmentSets()
	{
		return equipmentSets;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getGenderRef()
	 */
	@Override
	public ReferenceFacade<GenderFacade> getGenderRef()
	{
		return gender;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setGender(pcgen.cdom.enumeration.Gender)
	 */
	@Override
	public void setGender(GenderFacade gender)
	{
		theCharacter.setGender((Gender) gender);
		Gender newGender = charDisplay.getGenderObject();
		this.selectedGender = newGender.toString();
		this.gender.setReference(newGender);
		refreshLanguageList();
	}

	@Override
	public void setGender(String gender)
	{
		this.selectedGender = gender;
		if (charDisplay.getRace() != null)
		{
			for (GenderFacade raceGender : charDisplay.getRace().getGenders())
			{
				if (raceGender.toString().equals(gender))
				{
					setGender(raceGender);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getModTotal(pcgen.core.facade.StatFacade)
	 */
	@Override
	public int getModTotal(StatFacade stat)
	{
		if (stat instanceof PCStat && !charDisplay.isNonAbility((PCStat) stat))
		{
			return Integer.valueOf(theCharacter.getStatModFor((PCStat) stat));
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getScoreTotalRef(pcgen.core.facade.StatFacade)
	 */
	@Override
	public ReferenceFacade<Integer> getScoreBaseRef(StatFacade stat)
	{
		DefaultReferenceFacade<Integer> score = statScoreMap.get(stat);
		if (score == null)
		{
			score = new DefaultReferenceFacade<Integer>(theCharacter.getTotalStatFor((PCStat) stat));
			statScoreMap.put(stat, score);
		}
		return score;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getScoreBase(pcgen.core.facade.StatFacade)
	 */
	@Override
	public int getScoreBase(StatFacade stat)
	{
		if (!(stat instanceof PCStat))
		{
			return 0;
		}
		return theCharacter.getBaseStatFor((PCStat) stat);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getScoreTotalString(pcgen.core.facade.StatFacade)
	 */
	@Override
	public String getScoreTotalString(StatFacade stat)
	{
		if (!(stat instanceof PCStat))
		{
			return "";
		}
		if (charDisplay.isNonAbility((PCStat) stat))
		{
			return "*"; //$NON-NLS-1$
		}

		return SettingsHandler.getGame().getStatDisplayText(theCharacter.getTotalStatFor((PCStat) stat));
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getScoreRaceBonus(pcgen.core.facade.StatFacade)
	 */
	@Override
	public int getScoreRaceBonus(StatFacade stat)
	{
		if (!(stat instanceof PCStat))
		{
			return 0;
		}
		PCStat activeStat = (PCStat) stat;
		if (charDisplay.isNonAbility(activeStat))
		{
			return 0;
		}

		//return Integer.valueOf(currentStatAnalysis.getTotalStatFor(aStat) - currentStatAnalysis.getBaseStatFor(aStat));
		int rBonus = (int) theCharacter.getRaceBonusTo("STAT", activeStat.getAbb()); //$NON-NLS-1$
		rBonus += (int) theCharacter.getBonusDueToType("STAT", activeStat.getAbb(), "RACIAL");

		return rBonus;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getScoreOtherBonus(pcgen.core.facade.StatFacade)
	 */
	@Override
	public int getScoreOtherBonus(StatFacade stat)
	{
		if (!(stat instanceof PCStat))
		{
			return 0;
		}
		PCStat activeStat = (PCStat) stat;
		if (charDisplay.isNonAbility(activeStat))
		{
			return 0;
		}

		//return Integer.valueOf(currentStatAnalysis.getTotalStatFor(aStat) - currentStatAnalysis.getBaseStatFor(aStat));
		int iRace = (int) theCharacter.getRaceBonusTo("STAT", activeStat.getAbb()); //$NON-NLS-1$
		iRace += (int) theCharacter.getBonusDueToType("STAT", activeStat.getAbb(), "RACIAL");

		return theCharacter.getTotalStatFor(activeStat)
				- theCharacter.getBaseStatFor(activeStat) - iRace;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setScoreBase(pcgen.core.facade.StatFacade, int)
	 */
	@Override
	public void setScoreBase(StatFacade stat, int score)
	{
		DefaultReferenceFacade<Integer> facade = statScoreMap.get(stat);
		if (facade == null)
		{
			facade = new DefaultReferenceFacade<Integer>(score);
			statScoreMap.put(stat, facade);
		}

		PCStat pcStat = null;
		final int pcPlayerLevels = charDisplay.totalNonMonsterLevels();
		Collection<PCStat> pcStatList = charDisplay.getStatSet();
		for (PCStat aStat : pcStatList)
		{
			if (stat.getAbbreviation().equals(aStat.getAbbreviation()))
			{
				pcStat = aStat;
				break;
			}
		}
		if (pcStat == null)
		{
			Logging.errorPrint("Unexpected stat '" + stat + "' found - ignoring.");
			return;
		}

		// Checking for bounds, locked stats and pool points
		String errorMsg = validateNewStatBaseScore(score, pcStat, pcPlayerLevels);
		if (StringUtils.isNotBlank(errorMsg))
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME, errorMsg);
			return;
		}

		final int baseScore = charDisplay.getStat(pcStat);
		// Deal with a point pool based game mode where you buy skills and feats as well as stats
		if (Globals.getGameModeHasPointPool())
		{
			if (pcPlayerLevels > 0)
			{
				int poolMod = getPurchaseCostForStat(theCharacter, score)
						- getPurchaseCostForStat(theCharacter, baseScore);
				//
				// Adding to stat
				//
				if (poolMod > 0)
				{
					if (poolMod > theCharacter.getSkillPoints())
					{
						delegate.showErrorMessage(Constants.APPLICATION_NAME,
								LanguageBundle.getFormattedString("in_sumStatPoolEmpty", Globals //$NON-NLS-1$
										.getGameModePointPoolName()));
						return;
					}
				} else if (poolMod < 0)
				{
					if (theCharacter.getStatIncrease(pcStat, true) < Math.abs(score - baseScore))
					{
						delegate.showErrorMessage(Constants.APPLICATION_NAME,
								LanguageBundle.getString("in_sumStatStartedHigher")); //$NON-NLS-1$
						return;
					}
				}

				theCharacter.adjustFeats(-poolMod);
				showPointPool();
			}
		}

		theCharacter.setStat(pcStat, score);
		facade.setReference(score);
		theCharacter.saveStatIncrease(pcStat, score - baseScore, false);
		theCharacter.calcActiveBonuses();
		hpRef.setReference(theCharacter.hitPoints());
		refreshLanguageList();

		updateScorePurchasePool(true);
		if (charLevelsFacade != null)
		{
			charLevelsFacade.fireSkillBonusEvent(this, 0, true);
		}
	}

	/**
	 * Assess if the new score is valid for the stat.
	 * 
	 * @param score The new score being checked.
	 * @param pcStat The stats being checked
	 * @param pcPlayerLevels The number of non moster levels the character currently has.
	 * @return An error message if the score is not valid.
	 */
	private String validateNewStatBaseScore(int score, PCStat pcStat, final int pcPlayerLevels)
	{
		if (charDisplay.isNonAbility(pcStat))
		{
			return LanguageBundle.getString("in_sumCannotModifyANonAbility"); //$NON-NLS-1$
		} else if (score < pcStat.getSafe(IntegerKey.MIN_VALUE))
		{
			return LanguageBundle.getFormattedString("in_sumCannotLowerStatBelow", SettingsHandler.getGame() //$NON-NLS-1$
					.getStatDisplayText(pcStat.getSafe(IntegerKey.MIN_VALUE)));
		} else if (score > pcStat.getSafe(IntegerKey.MAX_VALUE))
		{
			return LanguageBundle.getFormattedString("in_sumCannotRaiseStatAbove", SettingsHandler.getGame() //$NON-NLS-1$
					.getStatDisplayText(pcStat.getSafe(IntegerKey.MAX_VALUE)));
		} else if ((pcPlayerLevels < 2) && SettingsHandler.getGame().isPurchaseStatMode())
		{
			final int maxPurchaseScore = SettingsHandler.getGame().getPurchaseScoreMax(theCharacter);

			if (score > maxPurchaseScore)
			{
				return LanguageBundle.getFormattedString("in_sumCannotRaiseStatAbovePurchase", SettingsHandler //$NON-NLS-1$
						.getGame().getStatDisplayText(maxPurchaseScore));
			}

			final int minPurchaseScore = SettingsHandler.getGame().getPurchaseScoreMin(theCharacter);

			if (score < minPurchaseScore)
			{
				return LanguageBundle.getFormattedString("in_sumCannotLowerStatBelowPurchase", SettingsHandler //$NON-NLS-1$
						.getGame().getStatDisplayText(minPurchaseScore));
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#rollStats()
	 */
	@Override
	public void rollStats()
	{
		GameMode game = (GameMode) dataSet.getGameMode();
		int rollMethod = game.getRollMethod();
		if (rollMethod == Constants.CHARACTER_STAT_METHOD_ROLLED && game.getCurrentRollingMethod() == null)
		{
			return;
		}
		if (rollMethod == Constants.CHARACTER_STAT_METHOD_USER)
		{
			// If a user asks to roll in user mode, set it to the current all same value.
			rollMethod = Constants.CHARACTER_STAT_METHOD_ALL_THE_SAME;
		}
		theCharacter.rollStats(rollMethod);
		//XXX This is here to stop the stat mod from being stale. Can be removed once we merge with CDOM
		theCharacter.calcActiveBonuses();
		refreshStatScores();
		updateScorePurchasePool(true);
	}

	private void refreshStatScores()
	{
		for (StatFacade stat : statScoreMap.keySet())
		{
			DefaultReferenceFacade<Integer> score = statScoreMap.get(stat);
			if (stat instanceof PCStat)
			{
				score.setReference(theCharacter.getTotalStatFor((PCStat) stat));
			}
		}
		if (charLevelsFacade != null)
		{
			charLevelsFacade.fireSkillBonusEvent(this, 0, true);
			charLevelsFacade.updateSkillsTodo();
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#isStatRollEnabled()
	 */
	@Override
	public boolean isStatRollEnabled()
	{
		return (charLevelsFacade.getSize() == 0);
	}

	/**
	 * Update the  
	 */
	private void showPointPool()
	{
		if (poolPointText == null)
		{
			return;
		}

		int poolPointsTotal = 0;

		for (PCLevelInfo pcl : charDisplay.getLevelInfo())
		{
			poolPointsTotal += pcl.getSkillPointsGained(theCharacter);
		}

		int poolPointsUsed = poolPointsTotal - theCharacter.getSkillPoints();

		poolPointText.setReference(Integer.toString(poolPointsUsed) + " / " + Integer.toString(poolPointsTotal)); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getSkillModifier(pcgen.core.facade.SkillFacade)
	 */
	@Override
	public int getSkillModifier(SkillFacade skill, CharacterLevelFacade level)
	{
		if (skill.getKeyStat() == null)
		{
			return 0;
		}

		for (StatFacade stat : statScoreMap.keySet())
		{
			if (skill.getKeyStat().equals(stat.getAbbreviation()))
			{
				return getModTotal(stat);
			}
		}

		return 0;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getSkillRanks(pcgen.core.facade.SkillFacade)
	 */
	@Override
	public float getSkillRanks(SkillFacade skill, CharacterLevelFacade finallevel)
	{
		return charLevelsFacade.getSkillRanks(finallevel, skill);
		//		float numRanks = 0.0f;
		//		for (CharacterLevelFacade level : pcClassLevels)
		//		{
		//			numRanks += charLevelsFacade.getSkillRanks(finallevel, skill);
		//			if (level == finallevel)
		//			{
		//				break;
		//			}
		//		}
		//		return numRanks;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getSkillTotal(pcgen.core.facade.SkillFacade)
	 */
	public int getSkillTotal(SkillFacade skill, CharacterLevelFacade level)
	{
		return (int) (Math.floor(getSkillRanks(skill, level)) + getSkillModifier(skill, level));
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getMaxRanks(pcgen.cdom.enumeration.SkillCost, pcgen.core.facade.CharacterLevelFacade)
	 */
	@Override
	public float getMaxRanks(SkillCost cost, CharacterLevelFacade level)
	{
		if (cost == null || level == null || !pcClassLevels.containsElement(level))
		{
			return 0.0f;
		}
		if (cost.getCost() == 0)
		{
			return Float.NaN;
		}
		return ((float) pcClassLevels.getIndexOfElement(level) + 4) / cost.getCost();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getUndoManager()
	 */
	@Override
	public UndoManager getUndoManager()
	{
		return undoManager;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getRaceRef()
	 */
	@Override
	public ReferenceFacade<RaceFacade> getRaceRef()
	{
		return race;
	}

	/**
	 * @return A reference to a list containing the character's race.
	 */
	@Override
	public ListFacade<RaceFacade> getRaceAsList()
	{
		return raceList;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setRace(pcgen.core.facade.RaceFacade)
	 */
	@Override
	public void setRace(RaceFacade race)
	{
		// TODO: We don't have a HP dialog implemented yet, so don't try to show it
		SettingsHandler.setShowHPDialogAtLevelUp(false);
		//SettingsHandler.setShowStatDialogAtLevelUp(false);
		int oldLevel = charLevelsFacade.getSize();

		if (race == null)
		{
			race = Globals.s_EMPTYRACE;
		}
		this.race.setReference(race);
		if (race instanceof Race && race != charDisplay.getRace())
		{
			Logging.log(Logging.INFO, charDisplay.getName()
				+ ": Setting race to " + race); //$NON-NLS-1$
			theCharacter.setRace((Race) race);
			raceList.clearContents();
			if (race != Globals.s_EMPTYRACE)
			{
				raceList.addElement(race);
			}
		}
		refreshLanguageList();
		if (selectedGender != null)
		{
			setGender(selectedGender);
		}
		refreshRaceRelatedFields();

		if (oldLevel != charLevelsFacade.getSize())
		{
			delegate.showLevelUpInfo(this, oldLevel);
		}
	}

	private void refreshRaceRelatedFields()
	{
		race.setReference(charDisplay.getRace());

		if (charDisplay.getRace() != null)
		{
			for (HandedFacade handsFacade : charDisplay.getRace().getHands())
			{
				if (handsFacade.toString().equals(charDisplay.getHanded()))
				{
					handedness.setReference(handsFacade);
					break;
				}
			}
			for (GenderFacade pcGender : race.getReference().getGenders())
			{
				if (pcGender.equals(charDisplay.getGenderObject()))
				{
					gender.setReference(pcGender);
					break;
				}
			}
		}
		refreshClassLevelModel();
		refreshStatScores();
		age.setReference(charDisplay.getAge());
		updateAgeCategoryForAge();
		refreshHeightWeight();
		characterAbilities.rebuildAbilityLists();
		currentXP.setReference(charDisplay.getXP());
		xpForNextlevel.setReference(charDisplay.minXPForNextECL());
		xpTableName.setReference(charDisplay.getXPTableName());
		hpRef.setReference(theCharacter.hitPoints());
		alignment.setReference(charDisplay.getPCAlignment());
		refreshAvailableTempBonuses();
		companionSupportFacade.refreshCompanionData();

		updateLevelTodo();
		buildAvailableDomainsList();
		spellSupportFacade.refreshAvailableKnownSpells();
		updateScorePurchasePool(false);
		refreshEquipment();

		if (charDisplay.getRace() == null || Constants.NONESELECTED.equals(charDisplay.getRace().getKeyName()))
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY, "Race", "in_irTodoRace", 100));
		} else
		{
			todoManager.removeTodo("in_irTodoRace");
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getTabNameRef()
	 */
	@Override
	public ReferenceFacade<String> getTabNameRef()
	{
		return tabName;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setTabName(java.lang.String)
	 */
	@Override
	public void setTabName(String name)
	{
		tabName.setReference(name);
		theCharacter.setTabName(name);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getNameRef()
	 */
	@Override
	public ReferenceFacade<String> getNameRef()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setName(java.lang.String)
	 */
	@Override
	public void setName(String name)
	{
		this.name.setReference(name);
		theCharacter.setName(name);
		if (isNewCharName(charDisplay.getName()))
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY, "Name", "in_sumTodoName", 1));
		} else
		{
			todoManager.removeTodo("in_sumTodoName");
		}
	}

	/**
	 * Check  whether the field should be output. 
	 * @param field The BiographyField to check export rules for.
	 * @return true if the field should be output, false if it may not be.
	 */
	@Override
	public boolean getExportBioField(BiographyField field)
	{
		return !charDisplay.getSuppressBioField(field);
	}

	/**
	 * Set whether the field should be output. 
	 * @param field The BiographyField to set export rules for.
	 * @param export Should the field be shown in output.
	 */
	@Override
	public void setExportBioField(BiographyField field, boolean export)
	{
		theCharacter.setSuppressBioField(field, !export);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<String> getSkinColorRef()
	{
		return skinColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSkinColor(String color)
	{
		skinColor.setReference(color);
		theCharacter.setSkinColor(color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<String> getHairColorRef()
	{
		return hairColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHairColor(String color)
	{
		hairColor.setReference(color);
		theCharacter.setHairColor(color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<String> getEyeColorRef()
	{
		return eyeColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEyeColor(String color)
	{
		eyeColor.setReference(color);
		theCharacter.setEyeColor(color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<Integer> getHeightRef()
	{
		return heightRef;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHeight(int height)
	{
		int heightInInches =
				Globals.getGameModeUnitSet().convertHeightFromUnitSet(height);
		heightRef.setReference(height);
		theCharacter.setHeight(heightInInches);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<Integer> getWeightRef()
	{
		return weightRef;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWeight(int weight)
	{
		int weightInPounds =
				(int) Globals.getGameModeUnitSet().convertWeightFromUnitSet(
					weight);
		weightRef.setReference(weight);
		theCharacter.setWeight(weightInPounds);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getDeityRef()
	 */
	@Override
	public ReferenceFacade<DeityFacade> getDeityRef()
	{
		return deity;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setDeity(pcgen.core.facade.DeityFacade)
	 */
	@Override
	public void setDeity(DeityFacade deity)
	{
		this.deity.setReference(deity);
		if (deity instanceof Deity)
		{
			theCharacter.setDeity((Deity) deity);
		}
		refreshLanguageList();
		buildAvailableDomainsList();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#addDomain(pcgen.core.facade.DomainFacade)
	 */
	@Override
	public void addDomain(DomainFacade domainFacade)
	{
		if (!(domainFacade instanceof DomainFacadeImpl))
		{
			return;
		}
		DomainFacadeImpl domainFI = (DomainFacadeImpl) domainFacade;
		Domain domain = domainFI.getRawObject();
		if (charDisplay.hasDomain(domain))
		{
			return;
		}

		if (!isQualifiedFor(domainFacade))
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME,
					LanguageBundle.getFormattedString("in_qualifyMess", domain.getDisplayName()));

			return;
		}

		// Check selected domains vs Max number allowed
		if (charDisplay.getDomainCount() >= theCharacter.getMaxCharacterDomains())
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME,
					LanguageBundle.getFormattedString("in_errorNoMoreDomains"));

			return;
		}

		if (!theCharacter.hasDefaultDomainSource())
		{
			// No source for the domain yet? Default to the last added class level
			int level = charDisplay.getLevelInfoSize();
			PCLevelInfo highestLevelInfo = charDisplay.getLevelInfo(level - 1);
			PCClass cls = theCharacter.getClassKeyed(highestLevelInfo.getClassKeyName()); 
			theCharacter.setDefaultDomainSource(new ClassSource(cls, highestLevelInfo.getClassLevel()));
		}

		if (theCharacter.addDomain(domain))
		{
			domains.addElement(domainFI);
			DomainApplication.applyDomain(theCharacter, domain);

			theCharacter.calcActiveBonuses();

			remainingDomains.setReference(theCharacter.getMaxCharacterDomains() - charDisplay.getDomainCount());
			updateDomainTodo();
			spellSupportFacade.refreshAvailableKnownSpells();
			companionSupportFacade.refreshCompanionData();
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getDomains()
	 */
	@Override
	public ListFacade<DomainFacade> getDomains()
	{
		return domains;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#removeDomain(pcgen.core.facade.DomainFacade)
	 */
	@Override
	public void removeDomain(DomainFacade domain)
	{
		domains.removeElement(domain);
		Domain dom = ((DomainFacadeImpl)domain).getRawObject();
		DomainApplication.removeDomain(theCharacter, dom);
		theCharacter.removeDomain(((DomainFacadeImpl) domain).getRawObject());
		remainingDomains.setReference(theCharacter.getMaxCharacterDomains() - charDisplay.getDomainCount());
		updateDomainTodo();
		spellSupportFacade.refreshAvailableKnownSpells();
	}

	/**
	 * Update the todo list to reflect the change in number of domains.
	 */
	private void updateDomainTodo()
	{
		if (remainingDomains.getReference() > 0)
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.DOMAINS, "Domains", "in_domTodoDomainsLeft", 120));
			todoManager.removeTodo("in_domTodoTooManyDomains");
		} else if (remainingDomains.getReference() < 0)
		{
			todoManager
					.addTodo(new TodoFacadeImpl(Tab.DOMAINS, "Domains", "in_domTodoTooManyDomains", 120));
			todoManager.removeTodo("in_domTodoDomainsLeft");
		} else
		{
			todoManager.removeTodo("in_domTodoDomainsLeft");
			todoManager.removeTodo("in_domTodoTooManyDomains");
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getMaxDomains()
	 */
	@Override
	public ReferenceFacade<Integer> getMaxDomains()
	{
		return maxDomains;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getRemainingDomainSelectionsRef()
	 */
	@Override
	public ReferenceFacade<Integer> getRemainingDomainSelectionsRef()
	{
		return remainingDomains;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAvailableDomains()
	 */
	@Override
	public ListFacade<DomainFacade> getAvailableDomains()
	{
		return availDomains;
	}

	/**
	 * This method returns all available domains, without filtering.
	 */
	private void buildAvailableDomainsList()
	{
		List<DomainFacadeImpl> availDomainList = new ArrayList<DomainFacadeImpl>();
		List<DomainFacadeImpl> selDomainList = new ArrayList<DomainFacadeImpl>();
		Deity pcDeity = charDisplay.getDeity();

		if (pcDeity != null)
		{
			for (CDOMReference<Domain> domainRef : pcDeity.getSafeListMods(Deity.DOMAINLIST))
			{
				Collection<AssociatedPrereqObject> assoc = pcDeity.getListAssociations(Deity.DOMAINLIST, domainRef);
				for (AssociatedPrereqObject apo : assoc)
				{
					for (Domain d : domainRef.getContainedObjects())
					{
						if (!isDomainInList(availDomainList, d))
						{
							availDomainList.add(new DomainFacadeImpl(d, apo.getPrerequisiteList()));
						}
					}
				}
			}
		}

		// Loop through the available prestige domains
		for (PCClass aClass : charDisplay.getClassList())
		{
			/*
			 * Need to do for the class, for compatibility, since level 0 is
			 * loaded into the class itself
			 */
			processDomainList(aClass, availDomainList);
			processAddDomains(aClass, availDomainList);
			for (int lvl = 0; lvl <= charDisplay.getLevel(aClass); lvl++)
			{
				PCClassLevel cl = charDisplay.getActiveClassLevel(aClass, lvl);
				processAddDomains(cl, availDomainList);
				processDomainList(cl, availDomainList);
			}
		}

		// Loop through the character's selected domains
		for (Domain d : charDisplay.getDomainSet())
		{
			DomainFacadeImpl domainFI = new DomainFacadeImpl(d);
			boolean found = false;
			for (DomainFacadeImpl row : availDomainList)
			{
				if (d.equals(row.getRawObject()))
				{
					domainFI = row;
					found = true;
					break;
				}
			}

			if (!found)
			{
				availDomainList.add(domainFI);
			}

			if (!isDomainInList(selDomainList, d))
			{
				selDomainList.add(domainFI);
			}
		}

		availDomains.updateContents(availDomainList);
		domains.updateContents(selDomainList);
		maxDomains.setReference(theCharacter.getMaxCharacterDomains());
		remainingDomains.setReference(theCharacter.getMaxCharacterDomains() - charDisplay.getDomainCount());
		updateDomainTodo();
	}

	/**
	 * Check if a domain is a list of domains, irrespective of prerequisites.
	 *  
	 * @param qualDomainList The list of domains with their prerequisites.
	 * @param qualDomain The domain to search for.
	 * @return tue if the domain is in the list 
	 */
	private boolean isDomainInList(List<DomainFacadeImpl> qualDomainList, Domain domain)
	{
		for (DomainFacadeImpl row : qualDomainList)
		{
			if (domain.equals(row.getRawObject()))
			{
				return true;
			}
		}
		return false;
	}

	private void processAddDomains(CDOMObject cdo, final List<DomainFacadeImpl> availDomainList)
	{
		Collection<CDOMReference<Domain>> domainRefs = cdo.getListMods(PCClass.ALLOWED_DOMAINS);
		if (domainRefs != null)
		{
			for (CDOMReference<Domain> ref : domainRefs)
			{
				Collection<AssociatedPrereqObject> assoc = cdo.getListAssociations(PCClass.ALLOWED_DOMAINS, ref);
				for (AssociatedPrereqObject apo : assoc)
				{
					for (Domain d : ref.getContainedObjects())
					{
						/*
						 * TODO This gate produces a rather interesting, and
						 * potentially wrong situation. What if two ADDDOMAINS
						 * exist with different PRE? Doesn't this fail?
						 */
						if (!isDomainInList(availDomainList, d))
						{
							availDomainList.add(new DomainFacadeImpl(d, apo.getPrerequisiteList()));
						}
					}
				}
			}
		}
	}

	private void processDomainList(CDOMObject obj, final List<DomainFacadeImpl> availDomainList)
	{
		for (QualifiedObject<CDOMSingleRef<Domain>> qo : obj.getSafeListFor(ListKey.DOMAIN))
		{
			CDOMSingleRef<Domain> ref = qo.getRawObject();
			Domain domain = ref.resolvesTo();
			if (!isDomainInList(availDomainList, domain))
			{
				availDomainList.add(new DomainFacadeImpl(domain, qo.getPrerequisiteList()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getEquipmentSetRef()
	 */
	@Override
	public ReferenceFacade<EquipmentSetFacade> getEquipmentSetRef()
	{
		return equipSet;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setEquipmentSet(pcgen.core.facade.EquipmentSetFacade)
	 */
	@Override
	public void setEquipmentSet(EquipmentSetFacade set)
	{
		EquipmentSetFacade oldSet = equipSet.getReference();
		if (oldSet != null)
		{
			oldSet.getEquippedItems().removeListListener(this);
			oldSet.getEquippedItems().removeEquipmentListListener(this);
		}
		if (set instanceof EquipmentSetFacadeImpl)
		{
			((EquipmentSetFacadeImpl)set).activateEquipSet();
		}
		equipSet.setReference(set);
		set.getEquippedItems().addListListener(this);
		set.getEquippedItems().addEquipmentListListener(this);
		refreshTotalWeight();
	}

	/**
	 * Regenerate the character's list of languages.
	 */
	void refreshLanguageList()
	{
		long startTime = new Date().getTime();
		List<Language> sortedLanguages = new ArrayList<Language>(charDisplay.getLanguageSet());
		Collections.sort(sortedLanguages);
		languages.updateContents(sortedLanguages);
		autoLanguagesCache = null;

		boolean allowBonusLangAfterFirst = Globals.checkRule(RuleConstants.INTBONUSLANG);
		boolean atFirstLvl = theCharacter.getTotalLevels() <= 1;

		int bonusLangMax = theCharacter.getBonusLanguageCount();
		
		currBonusLangs = new ArrayList<Language>();
		Ability a = Globals.getContext().ref.silentlyGetConstructedCDOMObject(Ability.class, AbilityCategory.LANGBONUS,
				"*LANGBONUS");
		List<String> currBonusLangNameList = theCharacter.getAssociationList(a);
		for (LanguageFacade langFacade : languages)
		{
			Language lang = (Language) langFacade;
			if (currBonusLangNameList.contains(lang.getKeyName()))
			{
				currBonusLangs.add(lang);
			}
		}
		int bonusLangRemain = bonusLangMax - currBonusLangs.size();
		if (!allowBonusLangAfterFirst && !atFirstLvl)
		{
			bonusLangRemain = 0;
		}
		numBonusLang.setReference(bonusLangRemain);
		if (bonusLangRemain > 0)
		{
			if (allowBonusLangAfterFirst)
			{
				todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY,
					"Languages", "in_sumTodoBonusLanguage", 110));
				todoManager.removeTodo("in_sumTodoBonusLanguageFirstOnly");
			}
			else
			{
				todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY,
					"Languages", "in_sumTodoBonusLanguageFirstOnly", 110));
				todoManager.removeTodo("in_sumTodoBonusLanguage");
			}
		}
		else
		{
			todoManager.removeTodo("in_sumTodoBonusLanguage");
			todoManager.removeTodo("in_sumTodoBonusLanguageFirstOnly");
		}

		int numSkillLangSelected = 0;
		int skillLangMax = 0;
		//TODO: Need to cope with multiple skill languages
		SkillFacade speakLangSkill = dataSet.getSpeakLanguageSkill();
		if (speakLangSkill != null)
		{
			Skill skill = (Skill) speakLangSkill;
			List<String> langList = theCharacter.getAssociationList(skill);
			numSkillLangSelected = langList.size();
			skillLangMax = SkillRankControl.getTotalRank(theCharacter, skill).intValue();
		}

		int skillLangRemain = skillLangMax - numSkillLangSelected;
		numSkillLang.setReference(skillLangRemain);
		if (skillLangRemain > 0)
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY,
				"Languages", "in_sumTodoSkillLanguage", 112));
		}
		else
		{
			todoManager.removeTodo("in_sumTodoSkillLanguage");
		}
		if (skillLangRemain < 0)
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY,
				"Languages", "in_sumTodoSkillLanguageTooMany", 112));
		}
		else
		{
			todoManager.removeTodo("in_sumTodoSkillLanguageTooMany");
		}
		
		long endTime = new Date().getTime();
		Logging.log(Logging.DEBUG, "refreshLanguageList took " + (endTime - startTime) + " ms.");
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getLanguages()
	 */
	@Override
	public ListFacade<LanguageFacade> getLanguages()
	{
		return languages;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListFacade<LanguageChooserFacade> getLanguageChoosers()
	{
		Ability a = Globals.getContext().ref.silentlyGetConstructedCDOMObject(Ability.class, AbilityCategory.LANGBONUS,
				"*LANGBONUS");
		DefaultListFacade<LanguageChooserFacade> chooserList = new DefaultListFacade<LanguageChooserFacade>();
		chooserList.addElement(new LanguageChooserFacadeImpl(this,
			LanguageBundle.getString("in_sumLangBonus"), a)); //$NON-NLS-1$

		SkillFacade speakLangSkill = dataSet.getSpeakLanguageSkill();
		if (speakLangSkill != null)
		{
			chooserList.addElement(new LanguageChooserFacadeImpl(this,
				LanguageBundle.getString("in_sumLangSkill"), //$NON-NLS-1$
				(Skill) speakLangSkill));
		}
		return chooserList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLanguage(LanguageFacade lang)
	{
		CDOMObject owner = getLaguageOwner(lang);
		if (owner == null)
		{
			return;
		}

		List<Language> availLangs = new ArrayList<Language>();
		List<Language> selLangs = new ArrayList<Language>();
		ChoiceManagerList<Language> choiceManager = ChooserUtilities.getChoiceManager(owner, theCharacter);
		choiceManager.getChoices(theCharacter, availLangs, selLangs);
		selLangs.remove(lang);
		choiceManager.applyChoices(theCharacter, selLangs);
	}

	/**
	 * Identify the object that the language is associated with. i.e. The rules 
	 * object that granted the ability to use the language. 
	 * @param lang The language to be found.
	 * @return The granting rules object, or null if none or automatic.
	 */
	private CDOMObject getLaguageOwner(LanguageFacade lang)
	{
		if (currBonusLangs.contains(lang))
		{
			return Globals.getContext().ref.silentlyGetConstructedCDOMObject(Ability.class, AbilityCategory.LANGBONUS,
					"*LANGBONUS");
		} else if (languages.containsElement(lang) && !isAutomatic(lang))
		{
			return (Skill) dataSet.getSpeakLanguageSkill();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getFileRef()
	 */
	@Override
	public ReferenceFacade<File> getFileRef()
	{
		return file;
	}

	@Override
	public void setFile(File file)
	{
		this.file.setReference(file);
		try
		{
			theCharacter.setFileName(file.getCanonicalPath());
		}
		catch (IOException e)
		{
			Logging.errorPrint("CharacterFacadeImpl.setFile failed for " + file, e);
			theCharacter.setFileName(file.getPath());
		}
	}

	
	/**
	 * Retrieve a copy of the current character suitable for export. This 
	 * attempts to minimise the expensive cloning function, by returning the 
	 * previously cloned character if the base character has not changed in 
	 * the meantime. 
	 * @return A copy of the current character.
	 */
	private synchronized PlayerCharacter getExportCharacter()
	{
		PlayerCharacter exportPc = lastExportChar;
		if (exportPc == null || theCharacter.getSerial() != lastExportCharSerial)
		{
			// Calling preparePCForOutput will mark export character as modified, so compare original character serial when checking for real changes
			// Get serial at beginning so we can detect if a change occurs during clone and preparePCForOutput
			lastExportCharSerial = theCharacter.getSerial();
			exportPc = (PlayerCharacter) theCharacter.clone();

			// Get the PC all up to date, (equipment and active bonuses etc)
			exportPc.preparePCForOutput();
			
			lastExportChar = exportPc;
			
			// It is possible another thread changed PC during export; log for now, the next export will rebuild
			int countSerialChanges = theCharacter.getSerial() - lastExportCharSerial;
			if (countSerialChanges > 0)
			{
				Logging.log(Logging.DEBUG,
					"Player character " + exportPc.getName() + " changed "
						+ countSerialChanges + " times during export.");
			}
		}
		return exportPc;
	}
	
	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#export(pcgen.io.ExportHandler, java.io.BufferedWriter)
	 */
	@Override
	public void export(ExportHandler theHandler, BufferedWriter buf)
	{
		final int maxRetries = 3;
		for (int i = 0; i < maxRetries; i++)
		{
			try
			{
				Logging.log(Logging.DEBUG, "Starting export at serial " + theCharacter.getSerial() + " to " + theHandler.getTemplateFile());
				PlayerCharacter exportPc =  getExportCharacter();
				//PlayerCharacter exportPc =  theCharacter;
				theHandler.write(exportPc, buf);
				Logging.log(Logging.DEBUG, "Finished export at serial " + theCharacter.getSerial() + " to " + theHandler.getTemplateFile());
				return;
			} catch (ConcurrentModificationException e)
			{
				Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
				for (Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet())
				{
					if (threadEntry.getValue().length > 1 )
					{
						StringBuilder sb = new StringBuilder("Thread: " + threadEntry.getKey() + "\n");
						for (StackTraceElement elem : threadEntry.getValue())
						{
							sb.append("  ");
							sb.append(elem.toString());
							sb.append("\n");
						}
						Logging.log(Logging.INFO, sb.toString());
					}
				}
				Logging.log(Logging.WARNING, "Retrying export after ConcurrentModificationException", e);
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e1)
				{
					Logging.errorPrint("Interrupted sleep - probably closing.");
					return;

				}
			}
		}
		Logging.errorPrint("Unable to export using "
			+ theHandler.getTemplateFile()
			+ " due to concurrent modifications.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultOutputSheet(boolean pdf, File outputSheet)
	{
		UIPropertyContext context = UIPropertyContext.getInstance();
		String outputSheetPath = outputSheet.getAbsolutePath();
		if (pdf)
		{
			context.setProperty(UIPropertyContext.DEFAULT_PDF_OUTPUT_SHEET, outputSheetPath);
		} else
		{
			context.setProperty(UIPropertyContext.DEFAULT_HTML_OUTPUT_SHEET, outputSheetPath);
		}
		if (context.getBoolean(UIPropertyContext.SAVE_OUTPUT_SHEET_WITH_PC))
		{
			if (pdf)
			{
				theCharacter.setSelectedCharacterPDFOutputSheet(outputSheetPath);
			} else
			{
				theCharacter.setSelectedCharacterHTMLOutputSheet(outputSheetPath);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultOutputSheet(boolean pdf)
	{
		UIPropertyContext context = UIPropertyContext.getInstance();
		if (context.getBoolean(UIPropertyContext.SAVE_OUTPUT_SHEET_WITH_PC))
		{
			String sheet;			
			if (pdf)
			{
				sheet = theCharacter.getSelectedCharacterPDFOutputSheet();
			}
			else
			{
				sheet = theCharacter.getSelectedCharacterHTMLOutputSheet();
			}
			if (StringUtils.isNotEmpty(sheet))
			{
				return sheet;
			}
		}

		if (pdf)
		{
			return context.getProperty(UIPropertyContext.DEFAULT_PDF_OUTPUT_SHEET);
		}
		return context.getProperty(UIPropertyContext.DEFAULT_HTML_OUTPUT_SHEET);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getHandedRef()
	 */
	@Override
	public ReferenceFacade<HandedFacade> getHandedRef()
	{
		return handedness;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setHanded(java.lang.String)
	 */
	@Override
	public void setHanded(HandedFacade handedness)
	{
		this.handedness.setReference(handedness);
		theCharacter.setHanded((Handed) handedness);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getPlayersNameRef()
	 */
	@Override
	public ReferenceFacade<String> getPlayersNameRef()
	{
		return playersName;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setPlayersName(java.lang.String)
	 */
	@Override
	public void setPlayersName(String name)
	{
		this.playersName.setReference(name);
		theCharacter.setPlayersName(name);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#isQualifiedFor(pcgen.core.facade.ClassFacade)
	 */
	@Override
	public boolean isQualifiedFor(ClassFacade c)
	{
		if (c instanceof PCClass)
		{
			return theCharacter.isQualified((PCClass) c);
		}
		return false;
	}

	@Override
	public UIDelegate getUIDelegate()
	{
		return delegate;
	}

	/**
	 * Save the character to disc using its filename. Note this method is not 
	 * part of the CharacterFacade and should only be used by the 
	 * ChracterManager class.
	 * 
	 * @throws NullPointerException 
	 * @throws IOException If the write fails
	 */
	public void save() throws NullPointerException, IOException
	{
		GameMode mode = (GameMode) dataSet.getGameMode();
		List<CampaignFacade> campaigns = ListFacades.wrap(dataSet.getCampaigns());
		(new PCGIOHandler()).write(theCharacter, mode, campaigns, file.getReference().getAbsolutePath());
		theCharacter.setDirty(false);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#isAutomatic(pcgen.core.facade.LanguageFacade)
	 */
	@Override
	public boolean isAutomatic(LanguageFacade language)
	{
		if (autoLanguagesCache == null)
		{
			autoLanguagesCache = charDisplay.getAutoLanguages();
		}
		return autoLanguagesCache.contains(language);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRemovable(LanguageFacade language)
	{
		if (isAutomatic(language))
		{
			return false;
		}
		if (currBonusLangs.contains(language))
		{
			boolean allowBonusLangAfterFirst = Globals.checkRule(RuleConstants.INTBONUSLANG);
			boolean atFirstLvl = theCharacter.getTotalLevels() <= 1;
			return allowBonusLangAfterFirst || atFirstLvl;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getCharacterLevelsFacade()
	 */
	@Override
	public CharacterLevelsFacade getCharacterLevelsFacade()
	{
		return charLevelsFacade;
	}

	@Override
	public DescriptionFacade getDescriptionFacade()
	{
		return descriptionFacade;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setXP(int)
	 */
	@Override
	public void setXP(final int xp)
	{
		if (xp == currentXP.getReference())
		{
			// We've already processed this change, most likely via the adjustXP method
			return;
		}
		theCharacter.setXP(xp);
		checkForNewLevel();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getXPRef()
	 */
	@Override
	public ReferenceFacade<Integer> getXPRef()
	{
		return currentXP;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#adjustXP(int)
	 */
	@Override
	public void adjustXP(final int xp)
	{
		int currVal = currentXP.getReference();
		int newVal = currVal + xp;
		theCharacter.setXP(newVal);
		checkForNewLevel();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getXPForNextLevelRef()
	 */
	@Override
	public ReferenceFacade<Integer> getXPForNextLevelRef()
	{
		return xpForNextlevel;
	}

	@Override
	public ReferenceFacade<String> getXPTableNameRef()
	{
		return xpTableName;
	}

	@Override
	public void setXPTable(String newTable)
	{

		xpTableName.setReference(newTable);
		theCharacter.setXPTable(newTable);
		checkForNewLevel();
	}

	private void checkForNewLevel()
	{
		currentXP.setReference(charDisplay.getXP());
		xpForNextlevel.setReference(charDisplay.minXPForNextECL());

		if (charDisplay.getXP() >= charDisplay.minXPForNextECL())
		{
			delegate.showInfoMessage(Constants.APPLICATION_NAME, SettingsHandler.getGame().getLevelUpMessage());
		}
		updateLevelTodo();
	}

	@Override
	public ReferenceFacade<String> getCharacterTypeRef()
	{
		return characterType;
	}

	@Override
	public void setCharacterType(String newType)
	{

		characterType.setReference(newType);
		theCharacter.setCharacterType(newType);
		theCharacter.calcActiveBonuses();
		
		// This can affect traits mainly.
		characterAbilities.rebuildAbilityLists();
	}

	@Override
	public ReferenceFacade<String> getPreviewSheetRef()
	{
		return previewSheet;
	}

	@Override
	public void setPreviewSheet(String newSheet)
	{
		previewSheet.setReference(newSheet);
		theCharacter.setPreviewSheet(newSheet);
	}

	@Override
	public ReferenceFacade<SkillFilter> getSkillFilterRef()
	{
		return skillFilter;
	}

	@Override
	public void setSkillFilter(SkillFilter newFilter)
	{
		skillFilter.setReference(newFilter);
		theCharacter.setSkillFilter(newFilter);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#setAge(int)
	 */
	@Override
	public void setAge(final int age)
	{
		if (age == this.age.getReference())
		{
			// We've already processed this change, most likely via the setAgeCategory method
			return;
		}

		theCharacter.setAge(age);
		this.age.setReference(age);
		updateAgeCategoryForAge();
		refreshStatScores();
		refreshLanguageList();
	}

	/**
	 * Update the character's age category based on their age.
	 */
	private void updateAgeCategoryForAge()
	{
		AgeSet ageSet = charDisplay.getAgeSet();
		if (ageSet != null)
		{
			String ageCatName = ageSet.getName();
			for (SimpleFacade ageCatFacade : ageCategoryList)
			{
				if (ageCatFacade.toString().equals(ageCatName))
				{
					ageCategory.setReference(ageCatFacade);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAgeRef()
	 */
	@Override
	public ReferenceFacade<Integer> getAgeRef()
	{
		return age;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getAgeCategories()
	 */
	@Override
	public ListFacade<SimpleFacade> getAgeCategories()
	{
		return ageCategoryList;
	}

	@Override
	public void setAgeCategory(final SimpleFacade ageCat)
	{
		if (ageCat == this.ageCategory.getReference())
		{
			// We've already processed this change, most likely via the setAge method
			return;
		}

		final Race pcRace = charDisplay.getRace();
		final String selAgeCat = ageCat.toString();

		if ((pcRace != null) && !pcRace.equals(Globals.s_EMPTYRACE))
		{
			if (selAgeCat != null)
			{
				final int idx = Globals.getBioSet().getAgeSetNamed(selAgeCat);

				if (idx >= 0)
				{
					ageCategory.setReference(ageCat);
					Globals.getBioSet().randomize("AGECAT" + Integer.toString(idx), theCharacter);
					age.setReference(charDisplay.getAge());
					ageCategory.setReference(ageCat);
					refreshStatScores();
					refreshLanguageList();
				}
			}
		}
	}

	@Override
	public ReferenceFacade<SimpleFacade> getAgeCategoryRef()
	{
		return ageCategory;
	}

	/**
	 * This method updates the purchase point pool and the stat total text. The 
	 * stat total text will be updated whether we are in purchase mode or not. 
	 * displayed 
	 * @param checkPurchasePoints boolean true if the pool should be checked
	 * for available points before doing the update.
	 */
	private void updateScorePurchasePool(boolean checkPurchasePoints)
	{
		int usedStatPool = getUsedStatPool();

		// Handle purchase mode for stats
		if (SettingsHandler.getGame().isPurchaseStatMode())
		{
			// Let them dink on stats at 0th or 1st PC levels
			if (canChangePurchasePool())
			{
				theCharacter.setCostPool(usedStatPool);
				theCharacter.setPoolAmount(usedStatPool);
			}

			final String bString = Integer.toString(theCharacter.getCostPool());
			//	int availablePool = SettingsHandler.getPurchaseModeMethodPool();
			int availablePool = theCharacter.getPointBuyPoints();
			if (availablePool < 0)
			{
				availablePool = RollingMethods.roll(SettingsHandler.getGame().getPurchaseModeMethodPoolFormula());
				theCharacter.setPointBuyPoints(availablePool);
			}

			if (availablePool != 0)
			{
				statTotalLabelText.setReference(LanguageBundle.getFormattedString("in_sumStatCost", SettingsHandler //$NON-NLS-1$
						.getGame().getPurchaseModeMethodName()));
				statTotalText.setReference(LanguageBundle.getFormattedString(
						"in_sumStatPurchaseDisplay", bString, availablePool)); //$NON-NLS-1$
				modTotalLabelText.setReference("");
				modTotalText.setReference("");
			}

			if (checkPurchasePoints && (availablePool != 0))
			{
				//
				// Let the user know that they've exceeded their goal, but allow them to keep going if they want...
				// Only do this at 1st level or lower
				//
				if (canChangePurchasePool() && (availablePool > 0) && (usedStatPool > availablePool))
				{
					delegate.showInfoMessage(Constants.APPLICATION_NAME, LanguageBundle.getFormattedString(
							"in_sumYouHaveExcededTheMaximumPointsOf",//$NON-NLS-1$
							String.valueOf(availablePool), SettingsHandler.getGame().getPurchaseModeMethodName()));
				}
			}
		}

		// Non-purchase mode for stats
		if (!SettingsHandler.getGame().isPurchaseStatMode() || (theCharacter.getPointBuyPoints() == 0))
		{
			int statTotal = 0;
			int modTotal = 0;

			for (PCStat aStat : charDisplay.getStatSet())
			{
				if (charDisplay.isNonAbility(aStat) || !aStat.getSafe(ObjectKey.ROLLED))
				{
					continue;
				}

				final int currentStat = theCharacter.getBaseStatFor(aStat);
				final int currentMod = theCharacter.getStatModFor(aStat);

				statTotal += currentStat;
				modTotal += currentMod;
			}

			statTotalLabelText.setReference(LanguageBundle.getString("in_sumStatTotalLabel")); //$NON-NLS-1$
			statTotalText
					.setReference(LanguageBundle.getFormattedString("in_sumStatTotal", Integer.toString(statTotal)));
			modTotalLabelText.setReference(LanguageBundle.getString("in_sumModTotalLabel"));
			modTotalText.setReference(LanguageBundle.getFormattedString("in_sumModTotal", Integer.toString(modTotal)));
		}

		if (charLevelsFacade.getSize() == 0
				&& (allAbilitiesAreZero() || (SettingsHandler.getGame().isPurchaseStatMode() && (theCharacter
						.getPointBuyPoints() != getUsedStatPool()))))
		{
			todoManager.addTodo(new TodoFacadeImpl(Tab.SUMMARY, "Ability Scores", "in_sumTodoStats", 50));
		} else
		{
			todoManager.removeTodo("in_sumTodoStats");
		}
	}

	/**
	 * Idenitfy if the character can stil change purchase pool values - spent 
	 * or available. This action is restricted by level. 
	 * @return true if the character is allowed to change the purchase pool
	 */
	public boolean canChangePurchasePool()
	{
		// This is a problem for races with non-0 level
		// adjustment so only count PC & NPC levels, not
		// monster levels XXX
		int pcPlayerLevels = charDisplay.totalNonMonsterLevels();

		int maxDiddleLevel;
		if (poolPointText != null)
		{
			maxDiddleLevel = 0;
		} else
		{
			maxDiddleLevel = 1;
		}
		return pcPlayerLevels <= maxDiddleLevel;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getStatTotalLabelTextRef()
	 */
	@Override
	public ReferenceFacade<String> getStatTotalLabelTextRef()
	{
		return statTotalLabelText;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getStatTotalTextRef()
	 */
	@Override
	public ReferenceFacade<String> getStatTotalTextRef()
	{
		return statTotalText;
	}

	/**
	 * @return A reference to the label text for the character's modifier total
	 */
	@Override
	public ReferenceFacade<String> getModTotalLabelTextRef()
	{
		return modTotalLabelText;
	}

	/**
	 * @return A reference to the text for the character's modifier total
	 */
	@Override
	public ReferenceFacade<String> getModTotalTextRef()
	{
		return modTotalText;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getTodoList()
	 */
	@Override
	public ListFacade<TodoFacade> getTodoList()
	{
		return todoManager.getTodoList();
	}

	/**
	 * @return the PlayerCharacter the facade is fronting for.
	 */
	PlayerCharacter getTheCharacter()
	{
		return theCharacter;
	}

	@Override
	public ReferenceFacade<Integer> getTotalHPRef()
	{
		return hpRef;
	}

	@Override
	public ReferenceFacade<Integer> getRollMethodRef()
	{
		return rollMethodRef;
	}

	@Override
	public void refreshRollMethod()
	{
		if (!canChangePurchasePool())
		{
			return;
		}
		GameMode game = (GameMode) dataSet.getGameMode();
		rollMethodRef.setReference(game.getRollMethod());
		if (SettingsHandler.getGame().isPurchaseStatMode())
		{
			int availablePool = RollingMethods.roll(SettingsHandler.getGame().getPurchaseModeMethodPoolFormula());
			theCharacter.setPointBuyPoints(availablePool);

			// Make sure all scores are within the valid range
			for (StatFacade stat : statScoreMap.keySet())
			{
				DefaultReferenceFacade<Integer> score = statScoreMap.get(stat);
				if (score.getReference() < SettingsHandler.getGame().getPurchaseScoreMin(theCharacter)
						&& stat instanceof PCStat)
				{
					setStatToPurchaseNeutral((PCStat) stat, score);
				}
			}

		}

		hpRef.setReference(theCharacter.hitPoints());
		updateScorePurchasePool(false);
	}

	/**
	 * Reset the stat score to the neutral value (usually 10) for 
	 * the point buy method.
	 * 
	 * @param pcStat The stata ebing adjusted.
	 * @param scoreRef The reference tothe current score.
	 */
	private void setStatToPurchaseNeutral(PCStat pcStat, DefaultReferenceFacade<Integer> scoreRef)
	{
		int newScore = SettingsHandler.getGame().getPurchaseModeBaseStatScore(theCharacter);
		if (StringUtils.isNotEmpty(validateNewStatBaseScore(newScore, pcStat, charDisplay.totalNonMonsterLevels())))
		{
			newScore = SettingsHandler.getGame().getPurchaseScoreMin(theCharacter);
			if (StringUtils
					.isNotEmpty(validateNewStatBaseScore(newScore, pcStat, charDisplay.totalNonMonsterLevels())))
			{
				return;
			}
		}

		theCharacter.setStat(pcStat, newScore);
		scoreRef.setReference(newScore);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void adjustFunds(BigDecimal modVal)
	{
		BigDecimal currFunds = theCharacter.getGold();
		theCharacter.setGold(currFunds.add(modVal));
		updateWealthFields();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFunds(BigDecimal newVal)
	{
		theCharacter.setGold(newVal);
		updateWealthFields();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<BigDecimal> getFundsRef()
	{
		return fundsRef;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFacade<BigDecimal> getWealthRef()
	{
		return wealthRef;
	}

	@Override
	public ReferenceFacade<GearBuySellFacade> getGearBuySellRef()
	{
		return gearBuySellSchemeRef;
	}

	@Override
	public void setGearBuySellRef(GearBuySellFacade gearBuySell)
	{
		gearBuySellSchemeRef.setReference(gearBuySell);
		GearBuySellScheme scheme = (GearBuySellScheme) gearBuySell;
		int rate = scheme.getBuyRate().intValue();
		SettingsHandler.setGearTab_BuyRate(rate);
		rate = scheme.getSellRate().intValue();
		SettingsHandler.setGearTab_SellRate(rate);
	}

	/**
	 * Update the wealth related fields.
	 */
	private void updateWealthFields()
	{
		fundsRef.setReference(theCharacter.getGold());
		wealthRef.setReference(theCharacter.totalValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAllowDebt(boolean allowDebt)
	{
		this.allowDebt = allowDebt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAllowDebt()
	{
		return allowDebt;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getPurchasedEquipment()
	 */
	@Override
	public EquipmentListFacade getPurchasedEquipment()
	{
		return purchasedEquip;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#addPurchasedEquipment(pcgen.core.facade.EquipmentFacade, int)
	 */
	@Override
	public void addPurchasedEquipment(EquipmentFacade equipment, int quantity, boolean customize)
	{
		if (equipment == null || quantity <= 0)
		{
			return;
		}

		//		int nextOutputIndex = 1;
		Equipment equipItemToAdjust = (Equipment) equipment;

		if (customize)
		{
			equipItemToAdjust = openCustomizer(equipItemToAdjust);
			if (equipItemToAdjust == null)
			{
				return;
			}
		}
		else
		{
			if (equipItemToAdjust.getSafe(ObjectKey.MOD_CONTROL).getModifiersRequired())
			{
				if (!hasBeenAdjusted(equipItemToAdjust))
				{
					delegate.showErrorMessage(Constants.APPLICATION_NAME,
						LanguageBundle
							.getString("in_igBuyMustCustomizeItemFirst")); //$NON-NLS-1$

					return;
				}
			}
		}
		Equipment updatedItem = theCharacter.getEquipmentNamed(equipItemToAdjust.getName());

		if (!canAfford(equipItemToAdjust, quantity, (GearBuySellScheme) gearBuySellSchemeRef.getReference()))
		{
			delegate.showInfoMessage(Constants.APPLICATION_NAME,
					LanguageBundle.getFormattedString("in_igBuyInsufficientFunds", quantity, //$NON-NLS-1$
							equipItemToAdjust.getName()));
			return;
		}

		if (updatedItem != null)
		{
			// item is already in inventory; update it
			final double prevQty = (updatedItem.qty() < 0) ? 0 : updatedItem.qty();
			final double newQty = prevQty + quantity;

			theCharacter.updateEquipmentQty(updatedItem, prevQty, newQty);
			Float qty = new Float(newQty);
			updatedItem.setQty(qty);
			purchasedEquip.setQuantity(equipment, qty.intValue());
		} else
		{
			// item is not in inventory; add it
			updatedItem = equipItemToAdjust.clone();

			if (updatedItem != null)
			{
				// Set the number carried and add it to the character
				Float qty = new Float(quantity);
				updatedItem.setQty(qty);
				theCharacter.addEquipment(updatedItem);
			}
			purchasedEquip.addElement(updatedItem, quantity);
		}

		// Update the PC and equipment
		double itemCost = calcItemCost(updatedItem, quantity, (GearBuySellScheme) gearBuySellSchemeRef.getReference());
		theCharacter.adjustGold(itemCost * -1);
		theCharacter.setCalcEquipmentList();
		theCharacter.setDirty(true);
		updateWealthFields();
	}

	private boolean hasBeenAdjusted(Equipment equipItemToAdjust)
	{
		Set<EquipmentModifier> allEqMods = new HashSet<EquipmentModifier>(equipItemToAdjust.getEqModifierList(true));
		allEqMods.addAll(equipItemToAdjust.getEqModifierList(false));
		for (EquipmentModifier eqMod : allEqMods)
		{
			if (!eqMod.isType(Constants.EQMOD_TYPE_BASEMATERIAL))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * This method is called to determine whether the character can afford to buy
	 * the requested quantity of an item at the rate selected.
	 * @param selected Equipment item being bought, used to determine the base price
	 * @param purchaseQty double number of the item bought
	 * @param gearBuySellScheme The scheme for buying and selling rates
	 *
	 * This method was overhauled March, 2003 by sage_sam as part of FREQ 606205
	 * @return true if it can be afforded
	 */
	private boolean canAfford(Equipment selected, double purchaseQty, GearBuySellScheme gearBuySellScheme)
	{
		final float currentFunds = theCharacter.getGold().floatValue();

		final double itemCost = calcItemCost(selected, purchaseQty, gearBuySellScheme);

		return allowDebt || (itemCost <= currentFunds);
	}

	private double calcItemCost(Equipment selected, double purchaseQty, GearBuySellScheme gearBuySellScheme)
	{
		if (selected == null)
		{
			return 0;
		}
		
		BigDecimal rate = purchaseQty >= 0 ? gearBuySellScheme.getBuyRate() : gearBuySellScheme.getSellRate();
		if (purchaseQty < 0 && selected.isSellAsCash())
		{
			rate = gearBuySellScheme.getCashSellRate();
		}

		return (purchaseQty * rate.intValue()) * (float) 0.01 * selected.getCost(theCharacter).floatValue();
	}

	private Equipment openCustomizer(Equipment aEq)
	{
		if (aEq == null)
		{
			return null;
		}

		Equipment newEquip = aEq.clone();
		if (!newEquip.containsKey(ObjectKey.BASE_ITEM))
		{
			newEquip.put(ObjectKey.BASE_ITEM, CDOMDirectSingleRef.getRef(aEq));
		}

		EquipmentBuilderFacadeImpl builder =
				new EquipmentBuilderFacadeImpl(newEquip, theCharacter, delegate);
		CustomEquipResult result =
				delegate.showCustomEquipDialog(this, builder);
		if (newEquip != null && result != CustomEquipResult.CANCELLED
			&& dataSet.getEquipment() instanceof DefaultListFacade<?>)
		{
			((DefaultListFacade<EquipmentFacade>) dataSet.getEquipment())
				.addElement(newEquip);
		}
		return result == CustomEquipResult.PURCHASE ? newEquip : null;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#removePurchasedEquipment(pcgen.core.facade.EquipmentFacade, int)
	 */
	@Override
	public void removePurchasedEquipment(EquipmentFacade equipment, int quantity)
	{
		if (equipment == null || quantity <= 0)
		{
			return;
		}

		Equipment equipItemToAdjust = (Equipment) equipment;

		Equipment updatedItem = theCharacter.getEquipmentNamed(equipItemToAdjust.getName());
		double numRemoved = 0;

		// see if item is already in inventory; update it
		if (updatedItem != null)
		{
			final double prevQty = (updatedItem.qty() < 0) ? 0 : updatedItem.qty();
			numRemoved = Math.min(quantity, prevQty);
			final double newQty = Math.max(prevQty - numRemoved, 0);

			if (newQty <= 0)
			{
				// completely remove item
				updatedItem.setNumberCarried(new Float(0));
				updatedItem.setLocation(EquipmentLocation.NOT_CARRIED);

				final Equipment eqParent = updatedItem.getParent();

				if (eqParent != null)
				{
					eqParent.removeChild(theCharacter, updatedItem);
				}

				theCharacter.removeEquipment(updatedItem);
				purchasedEquip.removeElement(updatedItem);
			} else
			{
				// update item count
				theCharacter.updateEquipmentQty(updatedItem, prevQty, newQty);
				Float qty = new Float(newQty);
				updatedItem.setQty(qty);
				updatedItem.setNumberCarried(qty);
				purchasedEquip.setQuantity(equipment, qty.intValue());
			}

			theCharacter.updateEquipmentQty(updatedItem, prevQty, newQty);
			Float qty = new Float(newQty);
			updatedItem.setQty(qty);
			updatedItem.setNumberCarried(qty);
		}

		// Update the PC and equipment
		double itemCost = calcItemCost(updatedItem, numRemoved * -1,
				(GearBuySellScheme) gearBuySellSchemeRef.getReference());
		theCharacter.adjustGold(itemCost * -1);
		theCharacter.setCalcEquipmentList();
		theCharacter.setDirty(true);
		updateWealthFields();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteCustomEquipment(EquipmentFacade eqFacade)
	{
		if (eqFacade == null || !(eqFacade instanceof Equipment))
		{
			return;
		}

		Equipment itemToBeDeleted = (Equipment) eqFacade;
		
		if (!itemToBeDeleted.isType(Constants.TYPE_CUSTOM))
		{
			return;
		}
		
		if (!delegate.showWarningConfirm(LanguageBundle
			.getString("in_igDeleteCustomWarnTitle"),//$NON-NLS-1$
			LanguageBundle.getFormattedString("in_igDeleteCustomWarning", //$NON-NLS-1$
				itemToBeDeleted)))
		{
			return;
		}
		
		removePurchasedEquipment(itemToBeDeleted, Integer.MAX_VALUE);
		Globals.getContext().ref.forget(itemToBeDeleted);
		
		if (dataSet.getEquipment() instanceof DefaultListFacade<?>)
		{
			((DefaultListFacade<EquipmentFacade>) dataSet.getEquipment())
				.removeElement(itemToBeDeleted);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#isQualifiedFor(pcgen.core.facade.EquipmentFacade)
	 */
	@Override
	public boolean isQualifiedFor(EquipmentFacade equipment)
	{
		final Equipment equip = (Equipment) equipment;
		final boolean accept = PrereqHandler.passesAll(equip.getPrerequisiteList(), theCharacter, equip);

		if (accept && (equip.isShield() || equip.isWeapon() || equip.isArmor()))
		{
			return theCharacter.isProficientWith(equip);
		}

		return accept;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getEquipmentSizedForCharacter(pcgen.core.facade.EquipmentFacade)
	 */
	@Override
	public EquipmentFacade getEquipmentSizedForCharacter(EquipmentFacade equipment)
	{
		final Equipment equip = (Equipment) equipment;
		final SizeAdjustment newSize = charDisplay.getSizeAdjustment();
		if (equip.getSizeAdjustment() == newSize || !Globals.canResizeHaveEffect(theCharacter, equip, null))
		{
			return equipment;
		}

		final String existingKey = equip.getKeyName();
		final String newKey = equip.createKeyForAutoResize(newSize);

		Equipment potential = Globals.getContext().ref.silentlyGetConstructedCDOMObject(Equipment.class, newKey);

		if (newKey.equals(existingKey))
		{
			return equipment;
		}

		// If we've already resized this piece of equipment to this size
		// on a previous occasion, just substitute that piece of equipment
		// in place of the selected equipment.
		if (potential != null)
		{
			return potential;
		}

		final String newName = equip.createNameForAutoResize(newSize);
		potential = Globals.getContext().ref.silentlyGetConstructedCDOMObject(Equipment.class, newName);

		if (potential != null)
		{
			return potential;
		}

		final Equipment newEq = equip.clone();

		if (!newEq.containsKey(ObjectKey.BASE_ITEM))
		{
			newEq.put(ObjectKey.BASE_ITEM, CDOMDirectSingleRef.getRef(equip));
		}

		newEq.setName(newName);
		newEq.put(StringKey.OUTPUT_NAME, newName);
		newEq.put(StringKey.KEY_NAME, newKey);
		newEq.resizeItem(theCharacter, newSize);
		newEq.removeType(Type.AUTO_GEN);
		newEq.removeType(Type.STANDARD);
		if (!newEq.isType(Constants.TYPE_CUSTOM))
		{
			newEq.addType(Type.CUSTOM);
		}

		Globals.getContext().ref.importObject(newEq);

		return newEq;
	}

	/**
	 * Whether we should automatically resize all purchased gear to match the 
	 * character's size.
	 * @return true if equipment should be auto resize.
	 */
	@Override
	public boolean isAutoResize()
	{
		return theCharacter.isAutoResize();
	}

	/**
	 * Update whether we should automatically resize all purchased gear to match  
	 * the character's size.
	 * 
	 * @param autoResize The new value for auto resize equipment option.
	 */
	@Override
	public void setAutoResize(boolean autoResize)
	{
		theCharacter.setAutoResize(autoResize);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#createEquipmentSet(java.lang.String)
	 */
	@Override
	public EquipmentSetFacade createEquipmentSet(String setName)
	{
		String id = EquipmentSetFacadeImpl.getNewIdPath(charDisplay, null);
		EquipSet eSet = new EquipSet(id, setName);
		theCharacter.addEquipSet(eSet);
		final EquipmentSetFacadeImpl facade =
				new EquipmentSetFacadeImpl(delegate, theCharacter, eSet,
					dataSet, purchasedEquip, todoManager, this);
		equipmentSets.addElement(facade);

		return facade;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#deleteEquipmentSet(pcgen.core.facade.EquipmentSetFacade)
	 */
	@Override
	public void deleteEquipmentSet(EquipmentSetFacade set)
	{
		if (set == null || !(set instanceof EquipmentSetFacadeImpl))
		{
			return;
		}
		EquipmentSetFacadeImpl setImpl = (EquipmentSetFacadeImpl) set;
		EquipSet eSet = setImpl.getEquipSet();

		theCharacter.delEquipSet(eSet);
		equipmentSets.removeElement(set);
	}

	@Override
	public ReferenceFacade<String> getCarriedWeightRef()
	{
		return carriedWeightRef;
	}

	@Override
	public ReferenceFacade<String> getLoadRef()
	{
		return loadRef;
	}

	@Override
	public ReferenceFacade<String> getWeightLimitRef()
	{
		return weightLimitRef;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.EquipmentListFacade.EquipmentListListener#quantityChanged(pcgen.core.facade.EquipmentListFacade.EquipmentListEvent)
	 */
	@Override
	public void quantityChanged(EquipmentListEvent e)
	{
		refreshTotalWeight();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.event.ListListener#elementAdded(pcgen.core.facade.event.ListEvent)
	 */
	@Override
	public void elementAdded(ListEvent<EquipmentFacade> e)
	{
		refreshTotalWeight();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.event.ListListener#elementRemoved(pcgen.core.facade.event.ListEvent)
	 */
	@Override
	public void elementRemoved(ListEvent<EquipmentFacade> e)
	{
		refreshTotalWeight();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.event.ListListener#elementsChanged(pcgen.core.facade.event.ListEvent)
	 */
	@Override
	public void elementsChanged(ListEvent<EquipmentFacade> e)
	{
		refreshTotalWeight();
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.event.ListListener#elementModified(pcgen.core.facade.event.ListEvent)
	 */
	@Override
	public void elementModified(ListEvent<EquipmentFacade> e)
	{
		refreshTotalWeight();
	}

	/**
	 * Refreshes the total weight by reading it from the current equipment set.  
	 */
	private void refreshTotalWeight()
	{
		String weight = Globals.getGameModeUnitSet().displayWeightInUnitSet(charDisplay.totalWeight().doubleValue());
		carriedWeightRef.setReference(weight);

		Load load = charDisplay.getLoadType();
		loadRef.setReference(CoreUtility.capitalizeFirstLetter(load.toString()));

		Float mult = SettingsHandler.getGame().getLoadInfo().getLoadMultiplier(load.toString());
		double limit = 0.0f;
		if (mult != null)
		{
			limit = charDisplay.getLoadToken(load.toString());
		}
		double lowerLimit = 0.0f;
		for (Load testLoad : Load.values())
		{
			double testLimit = charDisplay.getLoadToken(testLoad.toString());
			if (testLoad.compareTo(load) < 0 && testLimit > lowerLimit)
			{
				lowerLimit = testLimit;
			}
		}
		StringBuilder loadLimit = new StringBuilder(Globals.getGameModeUnitSet().displayWeightInUnitSet(lowerLimit));
		if (limit > 0)
		{
			loadLimit.append(" - ");
			loadLimit.append(Globals.getGameModeUnitSet().displayWeightInUnitSet(limit));
		} else
		{
			loadLimit.append("+ ");
		}
		loadLimit.append(Globals.getGameModeUnitSet().getWeightUnit());
		weightLimitRef.setReference(loadLimit.toString());
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterLevelsFacade.HitPointListener#hitPointsChanged(pcgen.core.facade.CharacterLevelsFacade.CharacterLevelEvent)
	 */
	@Override
	public void hitPointsChanged(CharacterLevelEvent e)
	{
		hpRef.setReference(theCharacter.hitPoints());
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getInfoFactory()
	 */
	@Override
	public InfoFactory getInfoFactory()
	{
		return infoFactory;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#isQualifiedFor(pcgen.core.facade.InfoFacade)
	 */
	@Override
	public boolean isQualifiedFor(InfoFacade infoFacade)
	{
		if (!(infoFacade instanceof PObject))
		{
			return false;
		}

		PObject pObj = (PObject) infoFacade;
		if (!theCharacter.isQualified(pObj))
		{
			return false;
		}
		
		if (infoFacade instanceof Kit)
		{
			Kit kit = (Kit) infoFacade;
			BigDecimal totalCost = kit.getTotalCostToBeCharged(theCharacter);
			if (totalCost != null)
			{
				if (theCharacter.getGold().compareTo(totalCost) < 0)
				{
					// Character cannto afford the kit
					return false;
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#isQualifiedFor(pcgen.core.facade.DomainFacade)
	 */
	@Override
	public boolean isQualifiedFor(DomainFacade domainFacade)
	{
		if (!(domainFacade instanceof DomainFacadeImpl))
		{
			return false;
		}

		DomainFacadeImpl domainFI = (DomainFacadeImpl) domainFacade;
		Domain domain = domainFI.getRawObject();
		if (!PrereqHandler.passesAll(domainFI.getPrerequisiteList(), theCharacter, domain)
				|| !theCharacter.isQualified(domain))
		{
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isQualifiedFor(TempBonusFacade tempBonusFacade)
	{
		if (!(tempBonusFacade instanceof TempBonusFacadeImpl))
		{
			return false;
		}

		TempBonusFacadeImpl tempBonus = (TempBonusFacadeImpl) tempBonusFacade;
		CDOMObject originObj = tempBonus.getOriginObj();
		if (!theCharacter.isQualified(originObj))
		{
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isQualifiedFor(SpellFacade spellFacade,
		ClassFacade classFacade)
	{
		if (!(spellFacade instanceof SpellFacadeImplem)
			|| !(classFacade instanceof PCClass))
		{
			return false;
		}

		SpellFacadeImplem spellFI = (SpellFacadeImplem) spellFacade;
		PCClass pcClass = (PCClass) classFacade;

		if (!theCharacter.isQualified(spellFI.getSpell()))
		{
			return false;
		}
		if (!spellFI.getCharSpell().isSpecialtySpell(theCharacter)
			&& SpellCountCalc.isProhibited(spellFI.getSpell(), pcClass,
				theCharacter))
		{
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isQualifiedFor(EquipmentFacade equipFacade, EquipModFacade eqModFacade)
	{
		if (!(equipFacade instanceof Equipment) || !(eqModFacade instanceof EquipmentModifier))
		{
			return false;
		}
		
		Equipment equip = (Equipment) equipFacade;
		EquipmentModifier eqMod = (EquipmentModifier) eqModFacade;
		
		//TODO: Handle second head
		return equip.canAddModifier(eqMod, true);
	}
	
	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#addTemplate(pcgen.core.facade.TemplateFacade)
	 */
	@Override
	public void addTemplate(TemplateFacade templateFacade)
	{
		if (templateFacade == null || !(templateFacade instanceof PCTemplate))
		{
			return;
		}

		PCTemplate template = (PCTemplate) templateFacade;

		if (!PrereqHandler.passesAll(template.getPrerequisiteList(), theCharacter, template))
		{
			return;
		}

		if (!charDisplay.hasTemplate(template))
		{
			Logging.log(Logging.INFO, charDisplay.getName()
				+ ": Adding template " + template); //$NON-NLS-1$
			int oldLevel = charLevelsFacade.getSize();
			if (theCharacter.addTemplate(template))
			{
				Logging.log(Logging.INFO, charDisplay.getName()
					+ ": Successful add of template " + template); //$NON-NLS-1$
				templates.addElement(template);
				refreshRaceRelatedFields();

				if (oldLevel != charLevelsFacade.getSize())
				{
					delegate.showLevelUpInfo(this, oldLevel);
				}
			}
			else
			{
				Logging.log(
						Logging.INFO,
						charDisplay.getName()
							+ ": Nope: Add template " + template + " failed because no selection was made"); //$NON-NLS-1$
			}
		}
		else
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME, LanguageBundle.getString("in_irHaveTemplate"));
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#removeTemplate(pcgen.core.facade.TemplateFacade)
	 */
	@Override
	public void removeTemplate(TemplateFacade templateFacade)
	{
		if (templateFacade == null || !(templateFacade instanceof PCTemplate))
		{
			return;
		}

		PCTemplate template = (PCTemplate) templateFacade;

		if (charDisplay.hasTemplate(template) && template.isRemovable())
		{
			theCharacter.removeTemplate(template);
			theCharacter.calcActiveBonuses();
			templates.removeElement(template);
		} else
		{
			delegate.showErrorMessage(Constants.APPLICATION_NAME, LanguageBundle.getString("in_irNotRemovable"));
		}
	}

	private void refreshTemplates()
	{
		Collection<PCTemplate> pcTemplates = charDisplay.getDisplayVisibleTemplateList();
		for (PCTemplate template : pcTemplates)
		{
			if (!templates.containsElement(template))
			{
				templates.addElement(template);
			}
		}
		for (Iterator<TemplateFacade> iterator = templates.iterator(); iterator.hasNext();)
		{
			PCTemplate pcTemplate = (PCTemplate) iterator.next();
			if (!pcTemplates.contains(pcTemplate))
			{
				iterator.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getTemplates()
	 */
	@Override
	public ListFacade<TemplateFacade> getTemplates()
	{
		return templates;
	}

	@Override
	public void addCharacterChangeListener(CharacterChangeListener listener)
	{
		//TODO: implement this
	}

	@Override
	public void removeCharacterChangeListener(CharacterChangeListener listener)
	{
		//TODO: implement this
	}

	/* (non-Javadoc)
	 * @see pcgen.core.facade.CharacterFacade#getSpellSupport()
	 */
	@Override
	public SpellSupportFacade getSpellSupport()
	{
		return spellSupportFacade;
	}

	@Override
	public ReferenceFacade<File> getPortraitRef()
	{
		return portrait;
	}

	@Override
	public void setPortrait(File file)
	{
		portrait.setReference(file);
		theCharacter.setPortraitPath(file == null ? null : file.getAbsolutePath());
	}

	@Override
	public ReferenceFacade<Rectangle> getThumbnailCropRef()
	{
		return cropRect;
	}

	@Override
	public void setThumbnailCrop(Rectangle rect)
	{
		cropRect.setReference(rect);
		theCharacter.setPortraitThumbnailRect(rect);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty()
	{
		return theCharacter.isDirty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompanionSupportFacade getCompanionSupport()
	{
		return companionSupportFacade;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCompanionType()
	{
		Follower master = charDisplay.getMaster();
		if (master != null)
		{
			return master.getType().getKeyName();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharacterStubFacade getMaster()
	{
		Follower master = charDisplay.getMaster();
		if (master == null)
		{
			return null;
		}
		CompanionNotLoaded stub = new CompanionNotLoaded(master.getName(), new File(master.getFileName()),
				master.getRace(), master.getType().getKeyName());
		CharacterFacade masterFacade = CharacterManager.getCharacterMatching(stub);
		if (masterFacade != null)
		{
			return masterFacade;
		}
		return stub;
	}

	/**
	 * Since Rectangles are modifiable we make sure that no references of the reference
	 * object are leaked to the outside world. This guarantees that the underlying reference
	 * object will not changed after it is set.
	 */
	private static class RectangleReference extends DefaultReferenceFacade<Rectangle> {

		/**
		 * Create a new reference based on the supplied rectangle.
		 * @param rect
		 */
		public RectangleReference(Rectangle rect) {
			this.object = rect == null ? null : (Rectangle) rect.clone();
		}

		@Override
		public Rectangle getReference()
		{
			Rectangle rect = object;
			if (rect != null)
			{
				rect = (Rectangle) rect.clone();
			}
			return rect;
		}

		@Override
		public void setReference(Rectangle rect)
		{
			if (ObjectUtils.equals(this.object, rect))
			{
				return;
			}
			if (rect != null)
			{
				rect = (Rectangle) rect.clone();
			}
			Rectangle old = this.object;
			this.object = rect;
			if (rect != null)
			{
				rect = (Rectangle) rect.clone();
			}
			fireReferenceChangedEvent(this, old, rect);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultListFacade<KitFacade> getKits()
	{
		return kitList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addKit(KitFacade obj)
	{
		if (obj == null || !(obj instanceof Kit))
		{
			return;
		}

		Kit kit = (Kit) obj;
		if (!theCharacter.isQualified(kit))
		{
			return;
		}

		Logging.log(Logging.INFO, charDisplay.getName()
			+ ": Testing kit " + kit); //$NON-NLS-1$
		List<BaseKit> thingsToAdd = new ArrayList<BaseKit>();
		List<String> warnings = new ArrayList<String>();
		kit.testApplyKit(theCharacter, thingsToAdd, warnings);

		//
		// See if user wants to apply the kit even though there were errors
		//

		if (!showKitWarnings(kit, warnings))
		{
			return;
		}

		// The user is applying the kit so use the real PC now.
		Logging.log(Logging.INFO, charDisplay.getName()
			+ ": Adding kit " + kit); //$NON-NLS-1$
		kit.processKit(theCharacter, thingsToAdd);
		kitList.addElement(obj);

		// Kits can upate most things so do a thorough refresh
		race.setReference(charDisplay.getRace());
		refreshRaceRelatedFields();
		name.setReference(charDisplay.getName());
		characterType.setReference(charDisplay.getCharacterType());

		// Deity and domains
		deity.setReference(charDisplay.getDeity());
		buildAvailableDomainsList();
		
		refreshStatScores();
	}

	/**
	 * 
	 */
	private void refreshEquipment()
	{
		fundsRef.setReference(theCharacter.getGold());
		wealthRef.setReference(theCharacter.totalValue());

		purchasedEquip.refresh(theCharacter.getEquipmentMasterList());
		initEquipSet();
	}

	/**
	 * Show the user any warnings from thekit application and get 
	 * their approval to continue.
	 * 
	 * @param kit The kit being applied.
	 * @param warnings The warnigns generated in the test application.
	 * @return true if the kit should be applied, false if not.
	 */
	private boolean showKitWarnings(Kit kit, List<String> warnings)
	{
		if (warnings.isEmpty())
		{
			return true;
		}

		HtmlInfoBuilder warningMsg = new HtmlInfoBuilder();

		warningMsg.append(LanguageBundle.getString("in_kitWarnStart")); //$NON-NLS-1$
		warningMsg.appendLineBreak();
		warningMsg.append("<UL>"); //$NON-NLS-1$
		for (String string : warnings)
		{
			warningMsg.appendLineBreak();
			warningMsg.append("<li>"); //$NON-NLS-1$
			warningMsg.append(string);
			warningMsg.append("</li>"); //$NON-NLS-1$
		}
		warningMsg.append("</UL>"); //$NON-NLS-1$
		warningMsg.appendLineBreak();
		warningMsg.append(LanguageBundle.getString("in_kitWarnEnd")); //$NON-NLS-1$

		return delegate.showWarningConfirm(kit.getDisplayName(), warningMsg.toString());
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
	public List<KitFacade> getAvailableKits()
	{
		List<KitFacade> kits = new ArrayList<KitFacade>();
		for (KitFacade obj : dataSet.getKits())
		{
			if (obj == null || !(obj instanceof Kit))
			{
				continue;
			}

			if (((Kit) obj).isVisible(theCharacter))
			{
				kits.add(obj);
			}

		}

		return kits;
	}

	@Override
	public VariableProcessor getVariableProcessor()
	{
		return theCharacter.getVariableProcessor();
	}

	@Override
	public Float getVariable(String variableString, boolean isMax)
	{
		return theCharacter.getVariable(variableString, isMax);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesCharacter(PlayerCharacter pc)
	{
		return theCharacter != null && theCharacter.equals(pc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyCharges(List<EquipmentFacade> targets)
	{
		List<Equipment> chargedEquip = new ArrayList<Equipment>();
		for (EquipmentFacade equipmentFacade : targets)
		{
			if (equipmentFacade instanceof Equipment && ((Equipment) equipmentFacade).getMaxCharges() > 0)
			{
				chargedEquip.add((Equipment) equipmentFacade);
			}
		}
		
		if (chargedEquip.isEmpty())
		{
			return;
		}
		
		for (Equipment equip : chargedEquip)
		{
			int selectedCharges = getSelectedCharges(equip);
			if (selectedCharges < 0)
			{
				return;
			}
			equip.setRemainingCharges(selectedCharges);
			purchasedEquip.modifyElement(equip);	
		}
	}
	
	private int getSelectedCharges(Equipment equip)
	{
		int minCharges = equip.getMinCharges();
		int maxCharges = equip.getMaxCharges();
		
		String selectedValue =
				delegate.showInputDialog(equip.toString(), LanguageBundle
					.getFormattedString("in_igNumCharges", //$NON-NLS-1$
						Integer.toString(minCharges),
						Integer.toString(maxCharges)), Integer.toString(equip
					.getRemainingCharges()));
		

		if (selectedValue == null)
		{
			return -1;
		}

		int charges;
		try
		{
			charges = Integer.parseInt(selectedValue.trim());
		}
		catch (NumberFormatException e) 
		{
			charges = minCharges-1;
		}
		if ((charges < minCharges) || (charges > maxCharges))
		{
			ShowMessageDelegate.showMessageDialog(LanguageBundle.getString("in_igValueOutOfRange"),
				Constants.APPLICATION_NAME, MessageType.ERROR);
			return getSelectedCharges(equip);
		}

		return charges;
	}

	@Override
	public void addNote(List<EquipmentFacade> targets)
	{
		List<Equipment> notedEquip = new ArrayList<Equipment>();
		for (EquipmentFacade equipmentFacade : targets)
		{
			if (equipmentFacade instanceof Equipment)
			{
				notedEquip.add((Equipment) equipmentFacade);
			}
		}
		
		if (notedEquip.isEmpty())
		{
			return;
		}
		
		for (Equipment equip : notedEquip)
		{
			String note = getNote(equip);
			if (note == null)
			{
				return;
			}
			equip.setNote(note);
			purchasedEquip.modifyElement(equip);	
		}
	}
	
	private String getNote(Equipment equip)
	{
		
		return delegate.showInputDialog(equip.toString(), LanguageBundle
					.getFormattedString("in_igEnterNote"), //$NON-NLS-1$
						equip.getNote());
	}
	
	/**
	 * The Class <code>LanguageListener</code> tracks adding and removal of 
	 * languages to the character.
	 */
	public class LanguageListener implements DataFacetChangeListener<Language>
	{
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dataAdded(DataFacetChangeEvent<Language> dfce)
		{
			if (dfce.getCharID() != theCharacter.getCharID())
			{
				return;
			}
			refreshLanguageList();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dataRemoved(DataFacetChangeEvent<Language> dfce)
		{
			if (dfce.getCharID() != theCharacter.getCharID())
			{
				return;
			}
			refreshLanguageList();
		}
		
	}
	
	/**
	 * The Class <code>TemplateListener</code> tracks adding and removal of 
	 * templates to the character.
	 */
	public class TemplateListener implements DataFacetChangeListener<Selection<PCTemplate, ?>>
	{
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dataAdded(DataFacetChangeEvent<Selection<PCTemplate, ?>> dfce)
		{
			if (dfce.getCharID() != theCharacter.getCharID())
			{
				return;
			}
			refreshTemplates();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dataRemoved(DataFacetChangeEvent<Selection<PCTemplate, ?>> dfce)
		{
			if (dfce.getCharID() != theCharacter.getCharID())
			{
				return;
			}
			refreshTemplates();
		}
		
	}
}
