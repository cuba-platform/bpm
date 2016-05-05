/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.bpm.gui.stencilset.helper;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.haulmont.bpm.BpmConstants;
import com.haulmont.bpm.entity.stencil.*;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class that is used by the stencilset editor to convert a stencilset JSON to the entities set and back.
 */
public class StencilSetJsonHelper {

    protected static final String[] BASIC_SERVICE_TASK_STENCIL_PROPERTY_PACKAGES = {"overrideidpackage", "namepackage", "servicetaskresultvariablepackage"};
    protected static final String DEFAULT_STENCIL_ICON = "activity/list/type.service.png";
    //${imageTag}
    protected static final String SERVICE_TASK_STENCIL_VIEW = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<svg\n   xmlns=\"http://www.w3.org/2000/svg\"\n   xmlns:svg=\"http://www.w3.org/2000/svg\"\n   xmlns:oryx=\"http://www.b3mn.org/oryx\"\n   xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n\n   width=\"102\"\n   height=\"82\"\n   version=\"1.0\">\n  <defs></defs>\n  <oryx:magnets>\n  \t<oryx:magnet oryx:cx=\"1\" oryx:cy=\"20\" oryx:anchors=\"left\" />\n  \t<oryx:magnet oryx:cx=\"1\" oryx:cy=\"40\" oryx:anchors=\"left\" />\n  \t<oryx:magnet oryx:cx=\"1\" oryx:cy=\"60\" oryx:anchors=\"left\" />\n  \t\n  \t<oryx:magnet oryx:cx=\"25\" oryx:cy=\"79\" oryx:anchors=\"bottom\" />\n  \t<oryx:magnet oryx:cx=\"50\" oryx:cy=\"79\" oryx:anchors=\"bottom\" />\n  \t<oryx:magnet oryx:cx=\"75\" oryx:cy=\"79\" oryx:anchors=\"bottom\" />\n  \t\n  \t<oryx:magnet oryx:cx=\"99\" oryx:cy=\"20\" oryx:anchors=\"right\" />\n  \t<oryx:magnet oryx:cx=\"99\" oryx:cy=\"40\" oryx:anchors=\"right\" />\n  \t<oryx:magnet oryx:cx=\"99\" oryx:cy=\"60\" oryx:anchors=\"right\" />\n  \t\n  \t<oryx:magnet oryx:cx=\"25\" oryx:cy=\"1\" oryx:anchors=\"top\" />\n  \t<oryx:magnet oryx:cx=\"50\" oryx:cy=\"1\" oryx:anchors=\"top\" />\n  \t<oryx:magnet oryx:cx=\"75\" oryx:cy=\"1\" oryx:anchors=\"top\" />\n  \t\n  \t<oryx:magnet oryx:cx=\"50\" oryx:cy=\"40\" oryx:default=\"yes\" />\n  </oryx:magnets>\n  <g pointer-events=\"fill\" oryx:minimumSize=\"50 40\">\n\t<rect id=\"text_frame\" oryx:anchors=\"bottom top right left\" x=\"1\" y=\"1\" width=\"94\" height=\"79\" rx=\"10\" ry=\"10\" stroke=\"none\" stroke-width=\"0\" fill=\"none\" />\n\t<rect id=\"bg_frame\" oryx:resize=\"vertical horizontal\" x=\"0\" y=\"0\" width=\"100\" height=\"80\" rx=\"10\" ry=\"10\" stroke=\"#bbbbbb\" stroke-width=\"1\" fill=\"#f9f9f9\" />\n\t\t<text \n\t\t\tfont-size=\"12\" \n\t\t\tid=\"text_name\" \n\t\t\tx=\"50\" \n\t\t\ty=\"40\" \n\t\t\toryx:align=\"middle center\"\n\t\t\toryx:fittoelem=\"text_frame\"\n\t\t\tstroke=\"#373e48\">\n\t\t</text>\n\t\n\t<g transform=\"translate(3,3)\">\n\t${imageTag}\n\t</g>\n  \n\t\n\t\n\t\n\t\n\t\n  </g>\n</svg>";
    //${iconUrl} ${iconId}
    protected static final String SVG_CUSTOM_ICON_TEMPLATE = "<image x=\"0\" y=\"0\" width=\"20\" height=\"20\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"${iconUrl}\" image-rendering=\"optimizeQuality\" id=\"${iconId}\"></image>";
    protected static final String SVG_DEFAULT_ICON_TEMPLATE = "<path anchors=\"top left\" style=\"fill:#72a7d0;stroke:none\" d=\"M 8,1 7.5,2.875 c 0,0 -0.02438,0.250763 -0.40625,0.4375 C 7.05724,3.330353 7.04387,3.358818 7,3.375 6.6676654,3.4929791 6.3336971,3.6092802 6.03125,3.78125 6.02349,3.78566 6.007733,3.77681 6,3.78125 5.8811373,3.761018 5.8125,3.71875 5.8125,3.71875 l -1.6875,-1 -1.40625,1.4375 0.96875,1.65625 c 0,0 0.065705,0.068637 0.09375,0.1875 0.002,0.00849 -0.00169,0.022138 0,0.03125 C 3.6092802,6.3336971 3.4929791,6.6676654 3.375,7 3.3629836,7.0338489 3.3239228,7.0596246 3.3125,7.09375 3.125763,7.4756184 2.875,7.5 2.875,7.5 L 1,8 l 0,2 1.875,0.5 c 0,0 0.250763,0.02438 0.4375,0.40625 0.017853,0.03651 0.046318,0.04988 0.0625,0.09375 0.1129372,0.318132 0.2124732,0.646641 0.375,0.9375 -0.00302,0.215512 -0.09375,0.34375 -0.09375,0.34375 L 2.6875,13.9375 4.09375,15.34375 5.78125,14.375 c 0,0 0.1229911,-0.09744 0.34375,-0.09375 0.2720511,0.147787 0.5795915,0.23888 0.875,0.34375 0.033849,0.01202 0.059625,0.05108 0.09375,0.0625 C 7.4756199,14.874237 7.5,15.125 7.5,15.125 L 8,17 l 2,0 0.5,-1.875 c 0,0 0.02438,-0.250763 0.40625,-0.4375 0.03651,-0.01785 0.04988,-0.04632 0.09375,-0.0625 0.332335,-0.117979 0.666303,-0.23428 0.96875,-0.40625 0.177303,0.0173 0.28125,0.09375 0.28125,0.09375 l 1.65625,0.96875 1.40625,-1.40625 -0.96875,-1.65625 c 0,0 -0.07645,-0.103947 -0.09375,-0.28125 0.162527,-0.290859 0.262063,-0.619368 0.375,-0.9375 0.01618,-0.04387 0.04465,-0.05724 0.0625,-0.09375 C 14.874237,10.52438 15.125,10.5 15.125,10.5 L 17,10 17,8 15.125,7.5 c 0,0 -0.250763,-0.024382 -0.4375,-0.40625 C 14.669647,7.0572406 14.641181,7.0438697 14.625,7 14.55912,6.8144282 14.520616,6.6141566 14.4375,6.4375 c -0.224363,-0.4866 0,-0.71875 0,-0.71875 L 15.40625,4.0625 14,2.625 l -1.65625,1 c 0,0 -0.253337,0.1695664 -0.71875,-0.03125 l -0.03125,0 C 11.405359,3.5035185 11.198648,3.4455201 11,3.375 10.95613,3.3588185 10.942759,3.3303534 10.90625,3.3125 10.524382,3.125763 10.5,2.875 10.5,2.875 L 10,1 8,1 z m 1,5 c 1.656854,0 3,1.3431458 3,3 0,1.656854 -1.343146,3 -3,3 C 7.3431458,12 6,10.656854 6,9 6,7.3431458 7.3431458,6 9,6 z\"></path>";
    protected static final String[] SERVICE_TASK_STENCIL_ROLES = {"Activity", "sequence_start", "sequence_end", "ActivitiesMorph", "all"};

    /**
     * Parses a stencilset JSON and converts it to the list of {@link Stencil} entities
     */
    public static List<Stencil> parseStencilSetJson(String stencilSetJson) {
        List<Stencil> allStencils  = new ArrayList<>();
        Map<String, GroupStencil> groupStencilsMap = new HashMap<>();
        Map<String, Integer> groupQtyMap = new HashMap<>();

        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(stencilSetJson);
        JsonArray stencils = rootElement.getAsJsonObject().getAsJsonArray("stencils");
        JsonArray propertyPackages = rootElement.getAsJsonObject().getAsJsonArray("propertyPackages");

        Gson gson = new Gson();
        Type stencilsCollectionType = new TypeToken<List<GsonStencil>>() {
        }.getType();
        List<GsonStencil> gsonStencils = gson.fromJson(stencils, stencilsCollectionType);

        Type propertyPackagesCollectionType = new TypeToken<List<GsonPropertyPackage>>() {
        }.getType();
        List<GsonPropertyPackage> gsonPropertyPackages = gson.fromJson(propertyPackages, propertyPackagesCollectionType);

        for (GsonStencil gsonStencil : gsonStencils) {
            Stencil stencil;

            GsonStencil.Custom custom = gsonStencil.getCustom();
            if (custom == null) {
                stencil = createStandardStencil(gsonStencil);
            } else {
                stencil = createServiceTaskStencil(gsonStencil, gsonPropertyPackages);
            }

            String groupName = gsonStencil.getGroups().get(0);
            GroupStencil groupStencil = groupStencilsMap.get(groupName);
            if (groupStencil == null) {
                groupStencil = new GroupStencil();
                groupStencil.setTitle(groupName);
                groupStencilsMap.put(groupName, groupStencil);
                groupQtyMap.put(groupName, 0);
                allStencils.add(groupStencil);
            }

            stencil.setParentGroup(groupStencil);
            groupQtyMap.put(groupName, groupQtyMap.get(groupName) + 1);
            stencil.setOrderNo(groupQtyMap.get(groupName));
            allStencils.add(stencil);
        }
        return allStencils;
    }

    protected static StandardStencil createStandardStencil(GsonStencil gsonStencil) {
        Metadata metadata = AppBeans.get(Metadata.class);
        StandardStencil stencil = metadata.create(StandardStencil.class);
        stencil.setStencilId(gsonStencil.getId());
        stencil.setTitle(gsonStencil.getTitle());
        stencil.setEditable(false);
        return stencil;
    }

    protected static ServiceTaskStencil createServiceTaskStencil(GsonStencil gsonStencil, List<GsonPropertyPackage> gsonPropertyPackages) {
        Metadata metadata = AppBeans.get(Metadata.class);
        GsonStencil.Custom custom = gsonStencil.getCustom();
        ServiceTaskStencil stencil = metadata.create(ServiceTaskStencil.class);
        stencil.setStencilId(gsonStencil.getId());
        stencil.setTitle(gsonStencil.getTitle());
        stencil.setBeanName(custom.getBeanName());
        stencil.setMethodName(custom.getMethodName());
        if (!Strings.isNullOrEmpty(gsonStencil.getCustomIconId())) {
            stencil.setIconFileId(UUID.fromString(gsonStencil.getCustomIconId()));
        }
        stencil.setEditable(true);
        List<ServiceTaskStencilMethodArg> args = new ArrayList<>();
        for (GsonStencil.MethodArg methodArg : custom.getMethodArgs()) {
            //todo MG what if propertyPackage doesn't exist
            gsonPropertyPackages.stream()
                    .filter(gsonPropertyPackage -> gsonPropertyPackage.getName().equals(methodArg.getPropertyPackageName()))
                    .findFirst()
                    .ifPresent(gsonPropertyPackage -> {
                        ServiceTaskStencilMethodArg serviceTaskStencilMethodArg = metadata.create(ServiceTaskStencilMethodArg.class);
                        serviceTaskStencilMethodArg.setStencil(stencil);
                        serviceTaskStencilMethodArg.setPropertyPackageName(methodArg.getPropertyPackageName());
                        GsonPropertyPackage.Property property = gsonPropertyPackage.getProperties().get(0);
                        serviceTaskStencilMethodArg.setPropertyPackageTitle(property.getTitle());
                        serviceTaskStencilMethodArg.setDefaultValue(property.getValue());
                        serviceTaskStencilMethodArg.setType(ServiceTaskStencilMethodArgType.fromCustomObjectType(methodArg.getType()));
                        args.add(serviceTaskStencilMethodArg);
                    });
        }
        stencil.setMethodArgs(args);
        return stencil;
    }

    /**
     * Generates a stecnilset JSON that contains custom stencils and their propertyPackages
     * @param stencils list of all stencils from the stencil editor
     * @throws IOException
     */
    public static String generateCustomStencilSet(Collection<Stencil> stencils) throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter dstJsonWriter = new JsonWriter(out);
        dstJsonWriter.beginObject();

        //propertypackages
        List<GsonPropertyPackage> serviceTaskGsonPropertyPackages = stencils.stream()
                .filter(stencil -> stencil instanceof ServiceTaskStencil)
                .flatMap(stencil -> ((ServiceTaskStencil) stencil).getMethodArgs().stream())
                .map(methodArg -> {
                    GsonPropertyPackage gsonPropertyPackage = new GsonPropertyPackage();
                    String propertyPackageName = createPropertyPackageNameFromMethodArg(methodArg);
                    gsonPropertyPackage.setName(propertyPackageName);

                    GsonPropertyPackage.Property gsonProperty = new GsonPropertyPackage.Property();
                    gsonProperty.setId(createPropertyPackageIdFromMethodArg(methodArg));
                    gsonProperty.setTitle(methodArg.getPropertyPackageTitle());
                    gsonProperty.setValue(methodArg.getDefaultValue());
                    gsonProperty.setType(methodArg.getType().propertyPackageType());
                    gsonProperty.setPopular(true);

                    gsonPropertyPackage.setProperties(Collections.singletonList(gsonProperty));

                    GsonPropertyPackage.Custom custom = new GsonPropertyPackage.Custom();
                    custom.setType(BpmConstants.CUSTOM_STENCIL_SERVICE_TASK);
                    gsonPropertyPackage.setCustom(custom);

                    return gsonPropertyPackage;
                })
                .collect(Collectors.toList());

        //stencils
        List<GsonStencil> gsonStencils = stencils.stream()
                .filter(stencil -> stencil instanceof ServiceTaskStencil)
                .map(stencil -> convertServiceTaskStencilToGsonStencil((ServiceTaskStencil) stencil))
                .collect(Collectors.toList());


        Gson gson = new Gson();

        String propertyPackagesJson = gson.toJson(serviceTaskGsonPropertyPackages);
        dstJsonWriter.name("propertyPackages").jsonValue(propertyPackagesJson);

        String stencilsJson = gson.toJson(gsonStencils);
        dstJsonWriter.name("stencils").jsonValue(stencilsJson);

        dstJsonWriter.endObject();
        dstJsonWriter.close();

        return out.toString();
    }

    protected static GsonStencil convertServiceTaskStencilToGsonStencil(ServiceTaskStencil serviceTaskStencil) {
        GsonStencil gsonStencil = new GsonStencil();
        gsonStencil.setType("node");
        gsonStencil.setId(serviceTaskStencil.getStencilId());
        gsonStencil.setTitle(serviceTaskStencil.getTitle());
        gsonStencil.setDescription("");
        UUID iconFileId;
        iconFileId = serviceTaskStencil.getIconFileId();
        if (iconFileId != null) {
            gsonStencil.setCustomIconId(iconFileId.toString());
        } else {
            gsonStencil.setIcon(DEFAULT_STENCIL_ICON);
        }
        gsonStencil.setView(createStencilView(iconFileId));

        gsonStencil.setGroups(Collections.singletonList(serviceTaskStencil.getParentGroup().getTitle()));

        List<String> propertyPackagesNames = new ArrayList<>(Arrays.asList(BASIC_SERVICE_TASK_STENCIL_PROPERTY_PACKAGES));
        List<String> propertyPackagesNamesFromMethodArgs = serviceTaskStencil.getMethodArgs().stream()
                .map(StencilSetJsonHelper::createPropertyPackageNameFromMethodArg)
                .collect(Collectors.toList());
        propertyPackagesNames.addAll(propertyPackagesNamesFromMethodArgs);
        gsonStencil.setPropertyPackages(propertyPackagesNames);
        gsonStencil.setMainPropertyPackages(propertyPackagesNamesFromMethodArgs);

        gsonStencil.setRoles(Arrays.asList(SERVICE_TASK_STENCIL_ROLES));
        GsonStencil.Custom custom = new GsonStencil.Custom();
        custom.setType(BpmConstants.CUSTOM_STENCIL_SERVICE_TASK);
        custom.setBeanName(serviceTaskStencil.getBeanName());
        custom.setMethodName(serviceTaskStencil.getMethodName());
        gsonStencil.setCustom(custom);
        for (ServiceTaskStencilMethodArg methodArg : serviceTaskStencil.getMethodArgs()) {
            GsonStencil.MethodArg gsonMethodArg = new GsonStencil.MethodArg();
            gsonMethodArg.setPropertyPackageName(createPropertyPackageNameFromMethodArg(methodArg));
            gsonMethodArg.setType(methodArg.getType().customObjectType());
            custom.getMethodArgs().add(gsonMethodArg);
        }

        return gsonStencil;
    }

    protected static String createStencilView(UUID iconId) {
        String imageTag = SVG_DEFAULT_ICON_TEMPLATE;
        if (iconId != null) {
            imageTag = SVG_CUSTOM_ICON_TEMPLATE.replace("${iconId}", iconId.toString());
            imageTag = imageTag.replace("${iconUrl}", "icon?f="  + iconId);
        }
        return SERVICE_TASK_STENCIL_VIEW.replace("${imageTag}", imageTag);
    }

    protected static String createPropertyPackageNameFromMethodArg(ServiceTaskStencilMethodArg methodArg) {
        return methodArg.getStencil().getStencilId().toLowerCase() + "-" + methodArg.getPropertyPackageTitle().toLowerCase() + "package";
    }

    protected static String createPropertyPackageIdFromMethodArg(ServiceTaskStencilMethodArg methodArg) {
        return methodArg.getStencil().getStencilId().toLowerCase() + "-" + methodArg.getPropertyPackageTitle().toLowerCase();
    }
}
