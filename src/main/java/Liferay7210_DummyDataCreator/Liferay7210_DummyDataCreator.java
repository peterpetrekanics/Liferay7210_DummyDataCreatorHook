package Liferay7210_DummyDataCreator;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetTagLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.dynamic.data.mapping.exception.StructureDuplicateStructureKeyException;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMStructureConstants;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.model.DDMTemplateConstants;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.persistence.DDMTemplateUtil;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.dynamic.data.mapping.util.DDMUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.configuration.ConfigurationFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.dynamic.data.mapping.model.DDMStructure;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author peterpetrekanics
 */
@Component
public class Liferay7210_DummyDataCreator {

	@Reference
	private UserLocalService _userLocalService;

	// Liferay lifecycle service
	@Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED)
	private ModuleServiceLifecycle _portalInitialized;

	@Activate
	public void createResourcesAtStartup() throws PortalException {

		_log.info(" ** Liferay7210_DummyDataCreator createResourcesAtStartup method initiating.. **");

		//Loading configuration values from the settings.properties file
		_configuration = ConfigurationFactoryUtil.getConfiguration(getClass().getClassLoader(), "settings");

		long companyId = PortalUtil.getDefaultCompanyId();
		_log.info("companyId: " + companyId);

		String shouldWeCleanUpFirst = _configuration.get("Clean_Up_Previously_Created_Resources");
		_log.info("shouldWeCleanUpFirst: " + shouldWeCleanUpFirst);


		String siteAdminUserCountString = _configuration.get("Site_Admin_User_count");
		_log.info("Site_Admin_user_count: " + siteAdminUserCountString);
		int siteAdminUserCount = Integer.parseInt(siteAdminUserCountString);
		if(siteAdminUserCount > 0) siteAdminUserCreator(shouldWeCleanUpFirst, companyId, siteAdminUserCount);

		String siteMemberUserCountString = _configuration.get("Site_Member_User_count");
		_log.info("Site_Member_user_count: " + siteMemberUserCountString);
		int siteMemberUserCount = Integer.parseInt(siteMemberUserCountString);
		if(siteMemberUserCount > 0) siteMemberUserCreator(shouldWeCleanUpFirst, companyId, siteMemberUserCount);

		String basicWebContentCountString = _configuration.get("Basic_Web_Content_count");
		_log.info("Basic_Web_Content_count: " + basicWebContentCountString);
		int basicWebContentCount = Integer.parseInt(basicWebContentCountString);
		if(basicWebContentCount > 0) basicWebContentCreator(shouldWeCleanUpFirst, companyId, basicWebContentCount);

		String plainStructureCountString = _configuration.get("Plain_Structure_count");
		_log.info("Plain_Structure_count: " + plainStructureCountString);
		int plainStructureCount = Integer.parseInt(plainStructureCountString);
		if(plainStructureCount > 0) plainStructureCreator(shouldWeCleanUpFirst, companyId, plainStructureCount);

		String fileCountString = _configuration.get("File_in_the_DM_count");
		_log.info("File_in_the_DM_count: " + fileCountString);
		int fileCount = Integer.parseInt(fileCountString);
		if(fileCount > 0) fileCreator(shouldWeCleanUpFirst, companyId, fileCount);

		String categoriesCountString = _configuration.get("Categories_count");
		_log.info("Categories_count: " + categoriesCountString);
		int categoriesCount = Integer.parseInt(categoriesCountString);
		if(categoriesCount > 0) categoryCreator(shouldWeCleanUpFirst, companyId, categoriesCount);

		String tagsCountString = _configuration.get("Tags_count");
		_log.info("Tags_count: " + tagsCountString);
		int tagsCount = Integer.parseInt(tagsCountString);
		if(tagsCount > 0) tagCreator(shouldWeCleanUpFirst, companyId, tagsCount);

		String formCountString = _configuration.get("Form_count");
		_log.info("Form_count: " + formCountString);
		int formCount = Integer.parseInt(formCountString);
		if(formCount > 0) formCreator(shouldWeCleanUpFirst, companyId, formCount);

		_log.info(" ** Liferay7210_DummyDataCreator createResourcesAtStartup method ends.. **");
	}

	private void siteAdminUserCreator(String shouldWeCleanUpFirst, long companyId, int siteAdminUserCount) throws PortalException {
		List<User> companyUsers = _userLocalService.getCompanyUsers(companyId, -1, -1);
		int currentDummyUserCount = 0;
		for(User thisCompanyUser : companyUsers) {
			_log.info("user screenname: " + thisCompanyUser.getScreenName());
			if(thisCompanyUser.getLastName().startsWith("dummy_siteadmin")) {
				if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
					try {
						_log.info("Following user will be deleted: " + thisCompanyUser.getScreenName());
						_userLocalService.deleteUser(thisCompanyUser);
					} catch (PortalException e) {
						e.printStackTrace();
					}
				} else {
					currentDummyUserCount++;
				}
			}
		}
		_log.info("dummyUserCount: " + currentDummyUserCount);
		
		String newAdminUserName = "dummy_siteadmin";
		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		String siteAdminRoleName = "Site Administrator";
		Role siteAdminRole = RoleLocalServiceUtil.getRole(companyId, siteAdminRoleName);
		long siteAdminRoleId = 0;
		if(siteAdminRole!=null) siteAdminRoleId = siteAdminRole.getRoleId();
		
		long currentUserId = 0;
		for(int i=1; i < siteAdminUserCount + 1; i++){
			_log.info("i: " + i);
			currentUserId = createUser(companyId, newAdminUserName, currentDummyUserCount+i, myGroupId, siteAdminRoleId);
			assignSiteRole(currentUserId, myGroupId, siteAdminRoleId);
		}
	}

	private void siteMemberUserCreator(String shouldWeCleanUpFirst, long companyId, int siteMemberUserCount) throws PortalException {
		List<User> companyUsers = _userLocalService.getCompanyUsers(companyId, -1, -1);
		int currentDummyUserCount = 0;
		for(User thisCompanyUser : companyUsers) {
			_log.info("user screenname: " + thisCompanyUser.getScreenName());
			if(thisCompanyUser.getLastName().startsWith("dummy_sitemember")) {
				if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
					try {
						_log.info("Following user will be deleted: " + thisCompanyUser.getScreenName());
						_userLocalService.deleteUser(thisCompanyUser);
					} catch (PortalException e) {
						e.printStackTrace();
					}
				} else {
					currentDummyUserCount++;
				}
			}
		}
		_log.info("dummyUserCount: " + currentDummyUserCount);
		
		String newMemberUserName = "dummy_sitemember";
		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		long siteMemberRoleId = 0;
		long currentUserId = 0;

		for(int i=1; i < siteMemberUserCount + 1; i++){
			_log.info("i: " + i);
			currentUserId = createUser(companyId, newMemberUserName, currentDummyUserCount+i, myGroupId, siteMemberRoleId);
		}
		_log.info("currentUserId: " + currentUserId);
	}

	private void basicWebContentCreator(String shouldWeCleanUpFirst, long companyId, int basicWebContentCount) throws PortalException {

		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		_log.info("myGroupId: " + myGroupId);
		int currentDummyArticleCount = 0;

		if(JournalArticleLocalServiceUtil.getArticlesCount(myGroupId)>0) {
			List<JournalArticle> journalArticles = JournalArticleLocalServiceUtil.getArticles(myGroupId, -1, -1);
			for(JournalArticle thisArtice : journalArticles) {
				if(thisArtice.getTitle().startsWith("dummy")) {
					if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
						try {
							_log.info("Following Article will be deleted: " + thisArtice.getTitle());
							JournalArticleLocalServiceUtil.deleteArticle(thisArtice);
						} catch (PortalException e) {
							e.printStackTrace();
						}
					} else {
						currentDummyArticleCount++;
					}
				}
			}
		}
		_log.info("currentDummyArticleCount: " + currentDummyArticleCount);

		int articleNumber = 0;
		for(int i = 1; i < basicWebContentCount + 1; i++){
			long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");
			long folderId = 0;
			articleNumber = currentDummyArticleCount + i;
			Map<Locale, String> titleMap = new HashMap<Locale, String>();
			titleMap.put(Locale.US, "dummy_Title" + articleNumber);
			Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
			descriptionMap.put(Locale.US, "Description");

			String content = "<ROOT></ROOT>";
			String ddmStructureKey = "BASIC-WEB-CONTENT";
			String ddmTemplateKey = "BASIC-WEB-CONTENT";
			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setScopeGroupId(myGroupId);
			JournalArticleLocalServiceUtil.addArticle(creatorUserId, myGroupId, folderId, titleMap, descriptionMap, content,
				ddmStructureKey, ddmTemplateKey, serviceContext);
		}
	}

	private void plainStructureCreator(String shouldWeCleanUpFirst, long companyId, int plainStructureCount) throws PortalException {

		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		int currentDummyStructureCount = 0;
		int currentDDMStructuresCount = 0;

		try {
			currentDDMStructuresCount = DDMStructureLocalServiceUtil.getDDMStructuresCount();
		} catch (Exception e) {
//			We are going to ignore the nullpointerexception which occurs at a clean database:
//			e.printStackTrace();
		}
		if(currentDDMStructuresCount > 0) {
			List<DDMStructure> ddmStructures = DDMStructureLocalServiceUtil.getStructures(myGroupId, -1, -1);

			for(DDMStructure thisddmStructure : ddmStructures) {
//				_log.info("thisddmStructure.getNameCurrentValue: " + thisddmStructure.getNameCurrentValue());
				if(thisddmStructure.getNameCurrentValue().startsWith("dummy")) {
					if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
						_log.info("Following Structure will be deleted: " + thisddmStructure.getNameCurrentValue());
						DDMStructureLocalServiceUtil.deleteDDMStructure(thisddmStructure);
					} else {
						currentDummyStructureCount++;
					}
				}
			}
		}

		List<DDMTemplate> ddmDDMTemplates = DDMTemplateLocalServiceUtil.getDDMTemplates(-1, -1);

		for(DDMTemplate thisddmTemplate : ddmDDMTemplates) {
			if(thisddmTemplate.getNameCurrentValue().startsWith("dummy")) {
				if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
					_log.info("Following Template will be deleted: " + thisddmTemplate.getNameCurrentValue());
					DDMTemplateLocalServiceUtil.deleteDDMTemplate(thisddmTemplate);
				}
			}
		}

		int structureNumber = 0;
		for(int i = 1; i < plainStructureCount + 1; i++){
			long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");
			long folderId = 0;
			structureNumber = currentDummyStructureCount + i;

			Map<Locale, String> titleMap = new HashMap<Locale, String>();
			titleMap.put(Locale.US, "dummy_Structure" + structureNumber);
			Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
			descriptionMap.put(Locale.US, "Description");

			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setScopeGroupId(myGroupId);
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);
			serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
			
			String mySerializedJSONDDMForm = "";
			mySerializedJSONDDMForm = "{\n" + 
					"    \"availableLanguageIds\": [\n" + 
					"        \"en_US\"\n" + 
					"    ],\n" + 
					"    \"defaultLanguageId\": \"en_US\",\n" + 
					"    \"fields\": [\n" + 
					"        {\n" + 
					"            \"label\": {\n" + 
					"                \"en_US\": \"Text\"\n" + 
					"            },\n" + 
					"            \"predefinedValue\": {\n" + 
					"                \"en_US\": \"\"\n" + 
					"            },\n" + 
					"            \"style\": {\n" + 
					"                \"en_US\": \"\"\n" + 
					"            },\n" + 
					"            \"tip\": {\n" + 
					"                \"en_US\": \"\"\n" + 
					"            },\n" + 
					"            \"dataType\": \"string\",\n" + 
					"            \"indexType\": \"keyword\",\n" + 
					"            \"localizable\": true,\n" + 
					"            \"name\": \"Text44j9\",\n" + 
					"            \"readOnly\": false,\n" + 
					"            \"repeatable\": false,\n" + 
					"            \"required\": false,\n" + 
					"            \"showLabel\": true,\n" + 
					"            \"type\": \"text\"\n" + 
					"        }\n" + 
					"    ]\n" + 
					"}";


			DDMForm ddmForm = null;
			try {
			    ddmForm = DDMUtil.getDDMForm(mySerializedJSONDDMForm);
			} catch (PortalException e) {
			    _log.error("Exception when parsing structure JSON", e);
			}

			DDMFormLayout ddmFormLayout = DDMUtil.getDefaultDDMFormLayout(ddmForm);
			long scopeClassNameId = PortalUtil.getPortal().getClassNameId(JournalArticle.class);
			long parentStructureId = DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID;
			String storageType = StorageType.JSON.toString();
			String structureKey = "dummy_structure" + structureNumber;

//			_log.info("structureKey: " + structureKey);
			long ddmStructureId = 0;
			try {
				DDMStructure ddmStructure = DDMStructureLocalServiceUtil.addStructure(
					creatorUserId, myGroupId, parentStructureId,
					scopeClassNameId, structureKey,
					titleMap, descriptionMap, ddmForm, ddmFormLayout, storageType,
					DDMStructureConstants.TYPE_DEFAULT, serviceContext);
				ddmStructureId = ddmStructure.getStructureId();
				_log.info("ddmStructureId: " + ddmStructureId);
			} catch (StructureDuplicateStructureKeyException e) {
				_log.info("Skipping creation of structure that already exists");
			} catch (PortalException e) {
				_log.error("Exception when creating structure: ", e);
			}
			
			String script = "${Text44j9.getData()}";
			long classPK = ddmStructureId;
			ClassName className = ClassNameLocalServiceUtil.getClassName("com.liferay.dynamic.data.mapping.model.DDMStructure");
			long classNameId = className.getClassNameId();
			long resourceClassNameId = PortalUtil.getClassNameId(JournalArticle.class);
			Map<Locale, String> tplNameMap = new HashMap<Locale, String>();
			tplNameMap.put(Locale.US, "dummy_Template" + structureNumber);
			Map<Locale, String> tplDescriptionMap = new HashMap<Locale, String>();
			tplDescriptionMap.put(Locale.US, "Description");
			String type = DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY;
			String mode = DDMTemplateConstants.TEMPLATE_MODE_CREATE;
			String language = TemplateConstants.LANG_TYPE_FTL;
			ServiceContext tplServiceContext = new ServiceContext();
			tplServiceContext.setScopeGroupId(myGroupId);
			tplServiceContext.setAddGroupPermissions(true);
			tplServiceContext.setAddGuestPermissions(true);
			tplServiceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

			try {
				DDMTemplateLocalServiceUtil.addTemplate(creatorUserId, myGroupId, classNameId, classPK, resourceClassNameId,
					tplNameMap, tplDescriptionMap, type, mode, language, script, tplServiceContext);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void fileCreator(String shouldWeCleanUpFirst, long companyId, int fileCount) throws PortalException {

		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		int currentDummyFileCount = 0;
		int currentFileCount = 0;
		long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");

		try {
			currentFileCount = DLFileEntryLocalServiceUtil.getGroupFileEntriesCount(myGroupId);
		} catch (Exception e) {
//			We are going to ignore the nullpointerexception which occurs at a clean database:
//			e.printStackTrace();
		}
		if(currentFileCount > 0) {
			List<DLFileEntry> currentDLFileEntries = DLFileEntryLocalServiceUtil.getGroupFileEntries(
				myGroupId, creatorUserId, -1, -1);

			for(DLFileEntry thisDLFileEntry : currentDLFileEntries) {
//				_log.info("thisddmStructure.getNameCurrentValue: " + thisddmStructure.getNameCurrentValue());
				if(thisDLFileEntry.getFileName().startsWith("dummy")) {
					if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
						_log.info("Following DLFileEntry will be deleted: " + thisDLFileEntry.getFileName());
						DLFileEntryLocalServiceUtil.deleteDLFileEntry(thisDLFileEntry);
					} else {
						currentDummyFileCount++;
					}
				}
			}
		}

	int fileNumber = 0;
	for(int i = 1; i < fileCount + 1; i++){
		fileNumber = currentDummyFileCount + i;
		long folderId = 0;
		String sourceFileName = "dummy" + fileNumber + ".txt";
		String mimeType = "";
		String title = "dummy" + fileNumber + ".txt";
		String description = "";
		String changeLog = "";
		
		String textFileBaseContent = "This is the test text file" + fileNumber;
		byte[] textFileBytes = textFileBaseContent.getBytes(StandardCharsets.UTF_8);
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(myGroupId);
		DLAppLocalServiceUtil.addFileEntry(creatorUserId, myGroupId, folderId, sourceFileName, mimeType,
			title, description, changeLog, textFileBytes, serviceContext);
		}
	}

	private void categoryCreator(String shouldWeCleanUpFirst, long companyId, int categoriesCount) throws PortalException {

		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		int currentDummyCategoryCount = 0;
		int currentCategoryCount = 0;
		long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");

		try {
			currentCategoryCount = AssetCategoryLocalServiceUtil.getAssetCategoriesCount();
		} catch (Exception e) {
//			We are going to ignore the nullpointerexception which occurs at a clean database:
//			e.printStackTrace();
		}
		if(currentCategoryCount > 0) {
			List<AssetCategory> currentAssetCategories = AssetCategoryLocalServiceUtil.getAssetCategories(-1, -1);

			for(AssetCategory thisAssetCategory : currentAssetCategories) {
//				_log.info("thisddmStructure.getNameCurrentValue: " + thisddmStructure.getNameCurrentValue());
				if(thisAssetCategory.getName().startsWith("dummy")) {
					if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
						_log.info("Following AssetCategory will be deleted: " + thisAssetCategory.getName());
						AssetCategoryLocalServiceUtil.deleteAssetCategory(thisAssetCategory);
					} else {
						currentDummyCategoryCount++;
					}
				}
			}
		}

		String vocabularyTitle = "dummy_vocabulary";
		long dummyVocabularyId = 0;
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(myGroupId);
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
//		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		List<AssetVocabulary> currentAssetCategories = AssetVocabularyLocalServiceUtil.getAssetVocabularies(-1, -1);
		for(AssetVocabulary thisAssetVocabulary : currentAssetCategories) {
			_log.info("thisAssetVocabulary.getTitle: " + thisAssetVocabulary.getTitle());
			if(thisAssetVocabulary.getTitle().startsWith("dummy")) {
				if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
					AssetVocabularyLocalServiceUtil.deleteAssetVocabulary(thisAssetVocabulary);
					_log.info("dummy vocab deleted");
					_log.info("dummyVocabularyId1: " + dummyVocabularyId);
				} else {
					dummyVocabularyId = thisAssetVocabulary.getVocabularyId();
					break;
				}
			}
		}
		AssetVocabulary dummyVocabulary = null;
		try {
			dummyVocabulary = AssetVocabularyLocalServiceUtil.addVocabulary(creatorUserId, myGroupId, vocabularyTitle, serviceContext);
			dummyVocabularyId = dummyVocabulary.getVocabularyId();
		} catch (Exception e) {
//			We are going to ignore the duplicate vocabulary exception, if it occurs:
//			e.printStackTrace();
		}
		_log.info("dummyVocabularyId: " + dummyVocabularyId);

		
		int categoryNumber = 0;
		for(int i = 1; i < categoriesCount + 1; i++){
			categoryNumber = currentDummyCategoryCount + i;
//			String categoryTitle = "dummy_category" + categoryNumber;
//			AssetCategoryLocalServiceUtil.addCategory(creatorUserId, myGroupId, categoryTitle, dummyVocabularyId, serviceContext);
			String[] categoryProperties = null;
			long parentCategoryId = 0;
			Map<Locale, String> titleMap = new HashMap<Locale, String>();
			titleMap.put(Locale.US, "dummy_Category" + categoryNumber);
			Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
			descriptionMap.put(Locale.US, "Description");
			AssetCategoryLocalServiceUtil.addCategory(creatorUserId, myGroupId, parentCategoryId, titleMap, descriptionMap,
				dummyVocabularyId, categoryProperties, serviceContext);
		}
	}

	private void tagCreator(String shouldWeCleanUpFirst, long companyId, int tagsCount) throws PortalException {

		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		int currentDummyTagCount = 0;
		int currentTagCount = 0;
		long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");

		try {
			currentTagCount = AssetTagLocalServiceUtil.getGroupTagsCount(myGroupId);
		} catch (Exception e) {
//			We are going to ignore the nullpointerexception which occurs at a clean database:
//			e.printStackTrace();
		}
		if(currentTagCount > 0) {
			List<AssetTag> currentAssetTags = AssetTagLocalServiceUtil.getAssetTags(-1, -1);

			for(AssetTag thisAssetTag : currentAssetTags) {
				if(thisAssetTag.getName().startsWith("dummy")) {
					if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
						_log.info("Following AssetTag will be deleted: " + thisAssetTag.getName());
						AssetTagLocalServiceUtil.deleteAssetTag(thisAssetTag);
					} else {
						currentDummyTagCount++;
					}
				}
			}
		}

	int assetTagNumber = 0;
	for(int i = 1; i < tagsCount + 1; i++){
		assetTagNumber = currentDummyTagCount + i;
		String name = "dummy_tag" + assetTagNumber;
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(myGroupId);
		try {
			AssetTag newTag = AssetTagLocalServiceUtil.addTag(creatorUserId, myGroupId, name, serviceContext);
		} catch (PortalException e) {
			e.printStackTrace();
		}
		}
	}

	private void formCreator(String shouldWeCleanUpFirst, long companyId, int formCount) throws PortalException {

		long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");
		String siteName = "Guest";
		long myGroupId = GroupLocalServiceUtil.getGroup(companyId, siteName).getGroupId();
		
		

		Map<Locale, String> titleMap = new HashMap<Locale, String>();
		titleMap.put(Locale.US, "dummy_StructureForForms");
		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
		descriptionMap.put(Locale.US, "Description");

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(myGroupId);
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		
		String mySerializedJSONDDMForm = "";
		mySerializedJSONDDMForm = "{\n" + 
				"	\"availableLanguageIds\" : [\n" + 
				"		\"en_US\"\n" + 
				"	],\n" + 
				"	\"defaultLanguageId\" : \"en_US\",\n" + 
				"	\"fields\" : [\n" + 
				"		{\n" + 
				"			\"autocomplete\" : false,\n" + 
				"			\"dataSourceType\" : \"manual\",\n" + 
				"			\"dataType\" : \"string\",\n" + 
				"			\"ddmDataProviderInstanceId\" : \"[]\",\n" + 
				"			\"ddmDataProviderInstanceOutput\" : \"[]\",\n" + 
				"			\"displayStyle\" : \"singleline\",\n" + 
				"			\"fieldNamespace\" : \"\",\n" + 
				"			\"indexType\" : \"keyword\",\n" + 
				"			\"label\" : {\n" + 
				"				\"en_US\" : \"DummyFormTextField\"\n" + 
				"			},\n" + 
				"			\"localizable\" : true,\n" + 
				"			\"name\" : \"DummyFormTextField\",\n" + 
				"			\"options\" : [\n" + 
				"				{\n" + 
				"					\"label\" : {\n" + 
				"						\"en_US\" : \"Option\"\n" + 
				"					},\n" + 
				"					\"value\" : \"Option\"\n" + 
				"				}\n" + 
				"			],\n" + 
				"			\"placeholder\" : {\n" + 
				"				\"en_US\" : \"\"\n" + 
				"			},\n" + 
				"			\"predefinedValue\" : {\n" + 
				"				\"en_US\" : \"\"\n" + 
				"			},\n" + 
				"			\"readOnly\" : false,\n" + 
				"			\"repeatable\" : false,\n" + 
				"			\"required\" : false,\n" + 
				"			\"showLabel\" : true,\n" + 
				"			\"tip\" : {\n" + 
				"				\"en_US\" : \"\"\n" + 
				"			},\n" + 
				"			\"tooltip\" : {\n" + 
				"				\"en_US\" : \"\"\n" + 
				"			},\n" + 
				"			\"type\" : \"text\",\n" + 
				"			\"visibilityExpression\" : \"\"\n" + 
				"		}\n" + 
				"	],\n" + 
				"	\"successPage\" : {\n" + 
				"		\"body\" : {\n" + 
				"			\"en_US\" : \"\"\n" + 
				"		},\n" + 
				"		\"enabled\" : false,\n" + 
				"		\"title\" : {\n" + 
				"			\"en_US\" : \"\"\n" + 
				"		}\n" + 
				"	}\n" + 
				"}";

		DDMForm ddmForm = null;
		try {
		    ddmForm = DDMUtil.getDDMForm(mySerializedJSONDDMForm);
		} catch (PortalException e) {
		    _log.error("Exception when parsing structure JSON", e);
		}

		DDMFormLayout ddmFormLayout = DDMUtil.getDefaultDDMFormLayout(ddmForm);
		long scopeClassNameId = PortalUtil.getPortal().getClassNameId(JournalArticle.class);
		long parentStructureId = DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID;
		String storageType = StorageType.JSON.toString();
		String structureKey = "dummy_formStructure";


		DDMStructure ddmStructure = null;
		try {
			ddmStructure = DDMStructureLocalServiceUtil.addStructure(
				creatorUserId, myGroupId, parentStructureId,
				scopeClassNameId, structureKey,
				titleMap, descriptionMap, ddmForm, ddmFormLayout, storageType,
				DDMStructureConstants.TYPE_DEFAULT, serviceContext);
		} catch (StructureDuplicateStructureKeyException e) {
			_log.info("Skipping creation of structure that already exists");
			ddmStructure = DDMStructureLocalServiceUtil.getStructure(myGroupId, scopeClassNameId, structureKey);
		} catch (PortalException e) {
			_log.error("Exception when creating structure: ", e);
		}
//		_log.info("ddmStructureId: " + ddmStructure.getStructureId());		

		List<DDMFormInstance> forms = DDMFormInstanceLocalServiceUtil.getDDMFormInstances(-1, -1);
		int currentDummyFormCount = 0;
		for(DDMFormInstance thisform : forms) {
			if(thisform.getNameCurrentValue().startsWith("dummy")) {
				if(shouldWeCleanUpFirst.equalsIgnoreCase("yes")) {
					_log.info("Following Form will be deleted: " + thisform.getNameCurrentValue());
					DDMFormInstanceLocalServiceUtil.deleteDDMFormInstance(thisform);
				} else {
					currentDummyFormCount++;
				}
			}
		}
		_log.info("currentDummyFormCount: " + currentDummyFormCount);
		
		
		
		int formNumber = 0;
		for(int i = 1; i < formCount + 1; i++){
			formNumber = currentDummyFormCount + i;
			Map<Locale, String> formNameMap = new HashMap<Locale, String>();
			formNameMap.put(Locale.US, "dummy_DDMFormInstance" + formNumber);
			Map<Locale, String> formDescriptionMap = new HashMap<Locale, String>();
			formDescriptionMap.put(Locale.US, "Description" + formNumber);

			DDMForm myDDMForm = ddmStructure.getDDMForm();
			DDMFormValues settingsDDMFormValues = new DDMFormValues(myDDMForm );
		
			DDMFormInstance newDDMFormInstance = DDMFormInstanceLocalServiceUtil.addFormInstance(creatorUserId, myGroupId, ddmStructure.getStructureId(), formNameMap,
				formDescriptionMap, settingsDDMFormValues, serviceContext);
		}
	}


	private long createUser(long companyId, String newAdminUserName, int i, long myGroupId, long siteAdminRoleId) throws PortalException {
		
		long creatorUserId = _userLocalService.getUserIdByEmailAddress(companyId, "test@liferay.com");
		long createdUserId = 0;
		boolean autoPassword = false;
		String password1 = "test";
		String password2 = "test";
		boolean autoScreenName = true;
		String screenName = newAdminUserName + i;
		String emailAddress = newAdminUserName + i + "@liferay.com";
		long facebookId = 0L;
		String openId = null;
		Locale locale = Locale.ENGLISH;
		String firstName = newAdminUserName + i;
		String middleName = "";
		String lastName = newAdminUserName + i;
		long prefixId = 0;
		long suffixId = 0;
		boolean male = true;
		int birthdayMonth = 1;
		int birthdayDay = 1;
		int birthdayYear = 1970;
		String jobTitle = null;
//		long[] groupIds = null;
		long[] groupIds = {myGroupId};
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;
		boolean sendEmail = false;
		ServiceContext serviceContext = new ServiceContext();
		createdUserId = _userLocalService.addUser(creatorUserId, companyId, autoPassword, password1, password2, autoScreenName, screenName,
				emailAddress, facebookId, openId, locale, firstName, middleName, lastName, prefixId, suffixId, male,
				birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds, roleIds, userGroupIds, sendEmail, serviceContext).getUserId();
		
		return createdUserId;
	}

	private void assignSiteRole(long currentUserId, long myGroupId, long siteAdminRoleId) {
		
		long[] siteAdminRoleIdArray = {siteAdminRoleId};
		UserGroupRoleLocalServiceUtil.addUserGroupRoles(currentUserId, myGroupId, siteAdminRoleIdArray );
		
	}


	private static Log _log = LogFactoryUtil.getLog(Liferay7210_DummyDataCreator.class.getName());

	private Configuration _configuration;

}