/*
 * Copyright (C) 2021 Rick Busarow
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

package tangle.sample.ui.fragmentsWithManualNavigation.breedList

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import tangle.sample.core.isMetric
import tangle.sample.data.breed.BreedSummary
import tangle.sample.ui.databinding.BreedItemBinding
import java.util.Locale

class BreedListAdapter(
  val onClick: (BreedSummary) -> Unit
) : PagingDataAdapter<BreedSummary, BreedViewHolder>(BreedSummaryComparator) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): BreedViewHolder {
    return BreedViewHolder(
      BreedItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      ),
      onClick
    )
  }

  override fun onBindViewHolder(holder: BreedViewHolder, position: Int) {
    val item = getItem(position)
    // Note that item may be null. ViewHolder must support binding a
    // null item as a placeholder.
    holder.bind(item)
  }
}

class BreedViewHolder(
  val itemBinding: BreedItemBinding,
  val onClick: (BreedSummary) -> Unit
) : RecyclerView.ViewHolder(itemBinding.root) {

  @SuppressLint("SetTextI18n")
  fun bind(item: BreedSummary?) {

    item ?: return

    itemBinding.name.text = item.name

    itemBinding.group.text = item.breedGroup

    if (Locale.getDefault().isMetric()) {

      itemBinding.height.text = "${item.heightMetric} cm"
      itemBinding.weight.text = "${item.weightMetric} kg"
    } else {
      itemBinding.height.text = "${item.heightImperial} inches"
      itemBinding.weight.text = "${item.weightImperial} pounds"
    }

    itemBinding.icon.load(item.imageUrl)

    itemBinding.root.setOnClickListener { onClick(item) }
  }
}

object BreedSummaryComparator : DiffUtil.ItemCallback<BreedSummary>() {
  override fun areItemsTheSame(
    oldItem: BreedSummary,
    newItem: BreedSummary
  ): Boolean = oldItem.id == newItem.id

  override fun areContentsTheSame(
    oldItem: BreedSummary,
    newItem: BreedSummary
  ): Boolean = oldItem == newItem
}
