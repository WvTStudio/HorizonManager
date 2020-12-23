package org.wvt.horizonmgr.ui

import androidx.compose.runtime.staticAmbientOf

@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
val AmbientStringResources = staticAmbientOf<StringResources>()

interface StringResources {
    val drawer_greeting: String
    val drawer_subtitle: String
    val drawer_item_recommend: String
    val drawer_item_package_manager: String
    val drawer_item_module_manager: String
    val drawer_item_online_resource: String
    val drawer_item_local_resource: String
    val drawer_item_community: String
    val drawer_item_join_game: String
    val drawer_item_join_group: String
    val drawer_item_donate: String
    val drawer_item_settings: String

    val recommend_appbar_title: String

    val package_manager_appbar_title: String
    val package_manager_menu_fresh: String
    val package_manager_item_detail: String
    val package_manager_item_delete: String
    val package_manager_item_clone: String
    val package_manager_item_rename: String
    val package_manager_item_no_description: String

    val module_manager_tab_icmod: String
    val module_manager_tab_icmap: String
    val module_manager_tab_mcmod: String
    val module_manager_tab_mcmap: String

    val module_manager_nopackage_tip: String

    val module_manager_icmod_empty_tip: String
    val module_manager_icmod_fab_install: String

    val module_manager_icmod_item_delete: String

    val module_manager_icmap_item_delete: String

    val online_resource_appbar_title: String
    val online_resource_search: String
    val online_resource_tune_source_global: String
    val online_resource_tune_source_chinese: String

    val online_resource_tune_sort_recommend: String
    val online_resource_tune_sort_time_asc: String
    val online_resource_tune_sort_time_desc: String
    val online_resource_tune_sort_name_asc: String
    val online_resource_tune_sort_name_desc: String

    val local_resource_appbar_title: String
    val local_resource_item_delete: String

    val community_appbar_title: String

    val donate_about: String
    val donate_about_title: String
    val donate_about_content: String
    val donate_alipay_startfailed: String
    val donate_wechat_starting: String
    val donate_wechat_startfailed: String
}

// TODO
object StringResourcesChinese: StringResources {
    override val drawer_greeting: String
        get() = TODO("Not yet implemented")
    override val drawer_subtitle: String
        get() = TODO("Not yet implemented")
    override val drawer_item_recommend: String
        get() = TODO("Not yet implemented")
    override val drawer_item_package_manager: String
        get() = TODO("Not yet implemented")
    override val drawer_item_module_manager: String
        get() = TODO("Not yet implemented")
    override val drawer_item_online_resource: String
        get() = TODO("Not yet implemented")
    override val drawer_item_local_resource: String
        get() = TODO("Not yet implemented")
    override val drawer_item_community: String
        get() = TODO("Not yet implemented")
    override val drawer_item_join_game: String
        get() = TODO("Not yet implemented")
    override val drawer_item_join_group: String
        get() = TODO("Not yet implemented")
    override val drawer_item_donate: String
        get() = TODO("Not yet implemented")
    override val drawer_item_settings: String
        get() = TODO("Not yet implemented")
    override val recommend_appbar_title: String
        get() = TODO("Not yet implemented")
    override val package_manager_appbar_title: String
        get() = TODO("Not yet implemented")
    override val package_manager_menu_fresh: String
        get() = TODO("Not yet implemented")
    override val package_manager_item_detail: String
        get() = TODO("Not yet implemented")
    override val package_manager_item_delete: String
        get() = TODO("Not yet implemented")
    override val package_manager_item_clone: String
        get() = TODO("Not yet implemented")
    override val package_manager_item_rename: String
        get() = TODO("Not yet implemented")
    override val package_manager_item_no_description: String
        get() = TODO("Not yet implemented")
    override val module_manager_tab_icmod: String
        get() = TODO("Not yet implemented")
    override val module_manager_tab_icmap: String
        get() = TODO("Not yet implemented")
    override val module_manager_tab_mcmod: String
        get() = TODO("Not yet implemented")
    override val module_manager_tab_mcmap: String
        get() = TODO("Not yet implemented")
    override val module_manager_nopackage_tip: String
        get() = TODO("Not yet implemented")
    override val module_manager_icmod_empty_tip: String
        get() = TODO("Not yet implemented")
    override val module_manager_icmod_fab_install: String
        get() = TODO("Not yet implemented")
    override val module_manager_icmod_item_delete: String
        get() = TODO("Not yet implemented")
    override val module_manager_icmap_item_delete: String
        get() = TODO("Not yet implemented")
    override val online_resource_appbar_title: String
        get() = TODO("Not yet implemented")
    override val online_resource_search: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_source_global: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_source_chinese: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_sort_recommend: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_sort_time_asc: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_sort_time_desc: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_sort_name_asc: String
        get() = TODO("Not yet implemented")
    override val online_resource_tune_sort_name_desc: String
        get() = TODO("Not yet implemented")
    override val local_resource_appbar_title: String
        get() = TODO("Not yet implemented")
    override val local_resource_item_delete: String
        get() = TODO("Not yet implemented")
    override val community_appbar_title: String
        get() = TODO("Not yet implemented")
    override val donate_about: String
        get() = TODO("Not yet implemented")
    override val donate_about_title: String
        get() = TODO("Not yet implemented")
    override val donate_about_content: String
        get() = TODO("Not yet implemented")
    override val donate_alipay_startfailed: String
        get() = TODO("Not yet implemented")
    override val donate_wechat_starting: String
        get() = TODO("Not yet implemented")
    override val donate_wechat_startfailed: String
        get() = TODO("Not yet implemented")
}