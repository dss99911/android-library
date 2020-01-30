/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.TaskSamples
import com.example.android.architecture.blueprints.todoapp.data.source.TaskRepository
import com.example.android.architecture.blueprints.todoapp.util.BaseViewModelTest
import com.example.android.architecture.blueprints.todoapp.util.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.await
import com.example.android.architecture.blueprints.todoapp.util.awaitData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import org.mockito.Mockito.spy

class TaskDetailViewModelTest : BaseViewModelTest() {
    val viewModel by inject<TaskDetailViewModel> {
        parametersOf(TaskDetailFragmentArgs.Builder(TaskSamples.sample1Active.id).build())
    }

    val repo by inject<TaskRepository>()

    @Test
    fun onRefresh() = runBlockingTest {
        //GIVEN no task
        //WHEN onResume
        viewModel.onRefresh()
        //THEN refresh occurs error
        assertThat(viewModel.task.await().isError()).isTrue()
        assertThat(viewModel.state.await().isError()).isTrue()

        //GIVEN same task
        repo.saveTask(TaskSamples.sample1Active)
        //WHEN onResume
        viewModel.onRefresh()
        //THEN refresh get task
        assertThat(viewModel.task.awaitData()).isEqualTo(TaskSamples.sample1Active)
    }


    @Test
    fun onCompleteChanged() = runBlockingTest {
        //GIVEN task
        repo.saveTask(TaskSamples.sample1Active)
        viewModel.onRefresh()

        //WHEN on change to complete
        viewModel.onCompleteChanged(true)

        //THEN changed to complete
        assertThat(repo.getTask(TaskSamples.sample1Active.id)!!.isCompleted).isTrue()

        //GIVEN task
        repo.saveTask(TaskSamples.sample2Completed)
        viewModel.onRefresh()

        //WHEN on change to Active
        viewModel.onCompleteChanged(false)

        //THEN change to active
        assertThat(repo.getTask(TaskSamples.sample1Active.id)!!.isActive).isTrue()
    }

    @Test
    fun onClickDelete() = runBlockingTest {
        //GIVEN has task
        repo.saveTask(TaskSamples.sample1Active)

        //WHEN on click delete
        val viewModel = spy(viewModel)
        viewModel.onClickDelete()

        //THEN delete and navigate to task fragment
        assertThat(repo.getTask(TaskSamples.sample1Active.id)).isNull()
        val directions =
            viewModel.captureNavigateDirection<TaskDetailFragmentDirections.ActionTaskDetailFragmentToTasksFragment>()
        assertThat(directions.userMessage).isEqualTo(DELETE_RESULT_OK)
    }

    @Test
    fun onClickEdit() = runBlockingTest {
        //GIVEN sample1Active
        //WHEN on click edit
        val viewModel = spy(viewModel)
        viewModel.onClickEdit()

        //THEN navigate to edit page
        val directions =
            viewModel.captureNavigateDirection<TaskDetailFragmentDirections.ActionTaskDetailFragmentToAddEditTaskFragment>()
        assertThat(directions.taskid).isEqualTo(TaskSamples.sample1Active.id)
    }


}