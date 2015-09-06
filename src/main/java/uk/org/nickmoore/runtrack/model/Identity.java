/*
 *  Copyright (C) 2013 Nick Moore
 *
 *  This file is part of ANR Run Tracker
 *
 *  ANR Run Tracker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.org.nickmoore.runtrack.model;

import android.content.Context;

import java.util.ArrayList;

import uk.org.nickmoore.runtrack.R;

/**
 * The current list of known Identities for Netrunner.
 */
@SuppressWarnings("WeakerAccess")
public enum Identity implements Stringable {
    NOISE(R.string.noise, Faction.ANARCH),
    WHIZZARD(R.string.whizzard, Faction.ANARCH),
    GABRIEL_SANTIAGO(R.string.gabe, R.string.gabe_short, Faction.CRIMINAL),
    ANDROMEDA(R.string.andromeda, R.string.andromeda_short, Faction.CRIMINAL),
    KATE_MAC_MACCAFFREY(R.string.kate, R.string.kate_short, Faction.SHAPER),
    CHAOS_THEORY(R.string.chaos_theory, R.string.chaos_theory_short, Faction.SHAPER),
    ENGINEERING_THE_FUTURE(R.string.engineering_the_future, R.string.engineering_the_future_short,
            Faction.HAAS_BIOROID),
    STRONGER_TOGETHER(R.string.stronger_together, R.string.stronger_together_short,
            Faction.HAAS_BIOROID),
    PERSONAL_EVOLUTION(R.string.personal_evolution, R.string.personal_evolution_short,
            Faction.JINTEKI),
    REPLICATING_PERFECTION(R.string.replicating_perfection, R.string.replicating_perfection_short,
            Faction.JINTEKI),
    MAKING_NEWS(R.string.making_news, Faction.NBN),
    THE_WORLD_IS_YOURS(R.string.the_world_is_yours, R.string.the_world_is_yours_short, Faction.NBN),
    BUILDING_A_BETTER_WORLD(R.string.building_a_better_world,
            R.string.building_a_better_world_short, Faction.WEYLAND),
    BECAUSE_WE_BUILT_IT(R.string.because_we_built_it, R.string.because_we_built_it_short,
            Faction.WEYLAND),
    THE_PROFESSOR(R.string.the_professor, R.string.the_professor_short, Faction.SHAPER),
    KIT(R.string.kit, R.string.kit_short, Faction.SHAPER),
    EXILE(R.string.exile, Faction.SHAPER),
    NEXT_DESIGN(R.string.next_design, Faction.HAAS_BIOROID),
    CEREBRAL_IMAGING(R.string.cerebral_imaging, R.string.cerebral_imaging_short,
            Faction.HAAS_BIOROID),
    CUSTOM_BIOTICS(R.string.custom_biotics, R.string.custom_biotics_short, Faction.HAAS_BIOROID),
    REINA_ROJA(R.string.reina_roja, R.string.reina_roja_short, Faction.ANARCH),
    THE_COLLECTIVE(R.string.the_collective, R.string.the_collective_short, Faction.SHAPER),
    LARAMY_FISK(R.string.laramy_fisk, R.string.laramy_fisk_short, Faction.CRIMINAL),
    GRNDL(R.string.grndl, Faction.WEYLAND),
    IAIN(R.string.iain_stirling, R.string.iain_stirling_short, Faction.CRIMINAL),
    KEN_TENMA(R.string.ken_express_tenma, R.string.ken_express_tenma_short, Faction.CRIMINAL),
    SILHOUETTE(R.string.silhouette, Faction.CRIMINAL),
    HARMONY_MEDTECH(R.string.harmony_medtech, R.string.harmony_medtech_short, Faction.JINTEKI),
    NISEI_DIVISION(R.string.nisei_division, R.string.nisei_division_short, Faction.JINTEKI),
    TENNIN_INSTITUTE(R.string.tennin_institute, R.string.tennin_institute_short, Faction.JINTEKI),
    SELECTIVE_JINTEKI(R.string.selective_mind_mapping, R.string.selective_mind_mapping_short,
            Faction.JINTEKI),
    SELECTIVE_HB(R.string.selective_mind_mapping, R.string.selective_mind_mapping_short,
            Faction.HAAS_BIOROID),
    BLUE_SUN(R.string.blue_sun, Faction.WEYLAND),
    NASIR_MEIDAN(R.string.nasir_meidan, R.string.nasir_meidan_short, Faction.SHAPER),
    LEELA_PATEL(R.string.leela_patel, R.string.leela_patel_short, Faction.CRIMINAL),
    GAGARIN(R.string.gagarin, R.string.gagarin_short, Faction.WEYLAND),
    NEAR_EARTH_HUB(R.string.near_earth_hub, R.string.near_earth_hub_short, Faction.NBN),
    EDWARD_KIM(R.string.edward_kim, R.string.edward_kim_short, Faction.ANARCH),
    THE_FOUNDRY(R.string.foundry, R.string.foundry_short, Faction.HAAS_BIOROID),
    QUETZAL(R.string.quetzal, Faction.ANARCH),
    INDUSTRIAL_GENOMICS(R.string.industrial_genomics, R.string.industrial_genomics_short, Faction.JINTEKI),
    ARGUS_SECURITY(R.string.argus_security, R.string.argus_security_short, Faction.WEYLAND),
    TITAN_TRANSNATIONAL(R.string.titan_transnational, R.string.titan_transnational_short, Faction.WEYLAND),
    MAXX(R.string.maxx, Faction.ANARCH),
    VALENCIA_ESTEVEZ(R.string.valencia_estevez, R.string.valencia_estevez_short, Faction.ANARCH),
    JINTEKI_BIOTECH(R.string.jinteki_biotech, Faction.JINTEKI),
    HAYLEY_KAPLAN(R.string.hayley_kaplan, R.string.hayley_kaplan_short, Faction.SHAPER),
    ADAM(R.string.adam, R.string.adam_short, Faction.NEUTRAL_RUNNER),
    APEX(R.string.apex, R.string.apex_short, Faction.NEUTRAL_RUNNER),
    GEIST(R.string.geist, R.string.geist_short, Faction.CRIMINAL),
    CYBERNETICS(R.string.cybernetics_div, R.string.cybernetics_div_short, Faction.HAAS_BIOROID),
    HAARPSICHORD(R.string.haarpsichord, R.string.haarpsichord_short, Faction.NBN),
    LIFE_IMAGINED(R.string.life_imagined, R.string.life_imagined_short, Faction.JINTEKI),
    NEW_ANGELES_SOL(R.string.new_angeles_sol, R.string.new_angeles_sol_short, Faction.NBN),
    SPARK(R.string.spark_agency, R.string.spark_agency_short, Faction.NBN),
    SUNNY(R.string.sunny_lebeau, R.string.sunny_lebeau_short, Faction.NEUTRAL_RUNNER),
    SYNC(R.string.sync, Faction.NBN);

    public final int textId;
    public final int shortTextId;
    public final Faction faction;

    private Identity(int textId, Faction faction) {
        this.textId = textId;
        this.shortTextId = textId;
        this.faction = faction;
    }

    private Identity(int textId, int shortTextId, Faction faction) {
        this.textId = textId;
        this.shortTextId = shortTextId;
        this.faction = faction;
    }

    @Override
    public CharSequence toCharSequence(Context context, boolean shortVersion) {
        if (shortVersion) {
            return faction.toCharSequence(context, true) + " - " + context.getText(shortTextId);
        }
        return faction.toCharSequence(context, false) + " - " + context.getText(textId);
    }

    public static Identity[] getIdentities(Faction faction) {
        ArrayList<Identity> idents = new ArrayList<Identity>();
        for(Identity ident: Identity.values()) {
            if(ident.faction == faction) {
                idents.add(ident);
            }
        }
        return idents.toArray(new Identity[idents.size()]);
    }

    public static Identity[] getIdentities(Role role) {
        ArrayList<Identity> idents = new ArrayList<Identity>();
        for(Identity ident: Identity.values()) {
            if(ident.faction.role == role) {
                idents.add(ident);
            }
        }
        return idents.toArray(new Identity[idents.size()]);
    }

}
