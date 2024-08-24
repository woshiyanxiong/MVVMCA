package com.ca.router_gradle_plugin;


import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;

/**
 * 类的作用的简要阐述
 * <p>
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/16/016</p>
 *
 * @author yanxiong
 */
public class MainPlugin implements Plugin<Project> {
    private static final Log logger = LogFactory.getLog(MainPlugin.class);

    @Override
    public void apply(Project project) {
        Boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);
        //only application module needs this plugin to generate register code
        if (isApp) {
//            project.getExtensions().findByType(AppExtension.class).registerTransform(new TestTransform());
            ExtensionContainer container = project.getExtensions();
            if (container!=null){
                AppExtension appExtension = container.findByType(AppExtension.class);
                if (appExtension != null) {
                    appExtension.registerTransform(new TestTransform());
                }
            }
        }

    }
}
