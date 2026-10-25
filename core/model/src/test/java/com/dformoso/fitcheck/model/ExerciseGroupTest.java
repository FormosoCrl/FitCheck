package com.dformoso.fitcheck.model;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.List;
import java.util.OptionalDouble;

public class ExerciseGroupTest {

    private static WorkoutSet set(String id, String exerciseId, String name, int reps) {
        return new WorkoutSet(
                id, "w1", exerciseId, name, 0, reps, OptionalDouble.empty(), false, false);
    }

    @Test
    public void emptyInputGivesNoGroups() {
        assertThat(ExerciseGroup.groupByExercise(List.of())).isEmpty();
        assertThat(ExerciseGroup.groupByExercise(null)).isEmpty();
    }

    @Test
    public void setsOfTheSameExerciseFallInOneGroup() {
        List<ExerciseGroup> groups = ExerciseGroup.groupByExercise(List.of(
                set("s1", "e1", "Dominadas", 4),
                set("s2", "e1", "Dominadas", 4),
                set("s3", "e1", "Dominadas", 4)));

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).exerciseName()).isEqualTo("Dominadas");
        assertThat(groups.get(0).setCount()).isEqualTo(3);
    }

    @Test
    public void groupsKeepTheOrderTheyFirstAppear() {
        List<ExerciseGroup> groups = ExerciseGroup.groupByExercise(List.of(
                set("s1", "e1", "Press banca", 9),
                set("s2", "e2", "Dominadas", 4)));

        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).exerciseName()).isEqualTo("Press banca");
        assertThat(groups.get(1).exerciseName()).isEqualTo("Dominadas");
    }

    /** Una superserie alterna ejercicios; deben quedar en dos grupos, no en cuatro. */
    @Test
    public void interleavedSetsStillGroupTogether() {
        List<ExerciseGroup> groups = ExerciseGroup.groupByExercise(List.of(
                set("s1", "e1", "Press banca", 9),
                set("s2", "e2", "Dominadas", 4),
                set("s3", "e1", "Press banca", 9),
                set("s4", "e2", "Dominadas", 4)));

        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).exerciseName()).isEqualTo("Press banca");
        assertThat(groups.get(0).setCount()).isEqualTo(2);
        assertThat(groups.get(1).setCount()).isEqualTo(2);
    }

    /** Mismo nombre pero ejercicios distintos: no se mezclan. */
    @Test
    public void differentExercisesWithTheSameNameStaySeparate() {
        List<ExerciseGroup> groups = ExerciseGroup.groupByExercise(List.of(
                set("s1", "e1", "Remo", 10),
                set("s2", "e2", "Remo", 10)));

        assertThat(groups).hasSize(2);
    }

    @Test
    public void aFailedAttemptAddsNoVolume() {
        // Contar un fallo como volumen infla el total con trabajo que no se hizo. Pero la
        // serie existe: haber intentado 80 de banca es informacion que hay que conservar.
        WorkoutSet sacada = new WorkoutSet(
                "s1", "w1", "e1", "Press banca", 0, 1, OptionalDouble.of(80), false, false);
        WorkoutSet fallada = new WorkoutSet(
                "s2", "w1", "e1", "Press banca", 1, 1, OptionalDouble.of(80), true, false);

        assertThat(sacada.volumeKg().isPresent()).isTrue();
        assertThat(sacada.volumeKg().getAsDouble()).isWithin(0.01).of(80);
        assertThat(fallada.volumeKg().isPresent()).isFalse();
    }

    @Test
    public void aFailedAttemptStillGroupsWithItsExercise() {
        WorkoutSet sacada = new WorkoutSet(
                "s1", "w1", "e1", "Press banca", 0, 5, OptionalDouble.of(60), false, false);
        WorkoutSet fallada = new WorkoutSet(
                "s2", "w1", "e1", "Press banca", 1, 1, OptionalDouble.of(80), true, false);

        List<ExerciseGroup> groups = ExerciseGroup.groupByExercise(List.of(sacada, fallada));
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).setCount()).isEqualTo(2);
    }

    @Test
    public void setsWithoutIdGroupByName() {
        List<ExerciseGroup> groups = ExerciseGroup.groupByExercise(List.of(
                set("s1", "", "Fondos", 12),
                set("s2", "", "Fondos", 12),
                set("s3", "", "Zancadas", 10)));

        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).setCount()).isEqualTo(2);
    }

    @Test
    public void aRecordStillCountsVolume() {
        // Un record SI cuenta: se saco. Es lo unico que lo diferencia de un fallo, que
        // tambien lleva marca propia pero no suma nada.
        WorkoutSet record = new WorkoutSet(
                "s1", "w1", "e1", "Press banca", 0, 1, OptionalDouble.of(85), false, true);

        assertThat(record.volumeKg().isPresent()).isTrue();
        assertThat(record.volumeKg().getAsDouble()).isWithin(0.01).of(85);
    }

    @Test
    public void aSetCannotBeFailedAndRecordAtOnce() {
        // Si las dos marcas conviven, la serie dice "no la saque" y "mi mejor marca" a la
        // vez, y no hay forma de pintarla ni de contarla.
        assertThrows(IllegalArgumentException.class, () -> new WorkoutSet(
                "s1", "w1", "e1", "Press banca", 0, 1, OptionalDouble.of(80), true, true));
    }
}
