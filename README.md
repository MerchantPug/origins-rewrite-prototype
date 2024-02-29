# Origins Rewrite Prototype
Chances are you probably found this repo with some clever digging, congrats.

Anyways, this is a co-joint project involving both eggohito and MerchantPug, meant as a proof of concept for what the planned rewrite to Origins could look like.

## Why is Origins rewriting.
I'm not Apace so I can't be certain as to what they are, so take this with a grain of salt.

Tl;dr, Origins just outgrew its foundations. There's also a lot of redundant content within the mod due to new additions and such, so rewriting from scratch is imo the way to go.
There's also huge optimisation concerns, with the mod performing worse each update due to the additions of new dynamic content each update. This isn't something we can completely avoid, but the rewrite is also planned to optimise the mod.

## Changes
- Calio, Apoli and Origins are now under one repo as submodules. This makes it far easier to work with.
- SerializableData is no longer the main way to handle Origins. We have moved to Codecs, the vanilla system for serialization.
  - Codecs are a far more powerful implementation when compared to SerializableData and is widely adopted within the Minecraft Java codebase.
  - This opens up many opportunities such as the following.
    - The use of dynamic registries.
    - Datagen.

## Project Specific Outlines
### Calio
Calio within the rewrite has pretty much gained a new purpose.

Calio is still where any miscellaneous helping tools will go, such as allowing recipes to serialize, but its main function is to house any extensions relating to data generation and codecs.

### Apoli
Apoli is pretty much the same as before when it comes to what's going to be put into it. Powers, actions and conditions will all be a part of Apoli.

### Origins
Origins is still the main way users will be able to interface with the other two mods.

The prototype does not implement any of the base mod's origins, as this is purely a proof of concept. Instead opting to implement two test Origins that you can switch to and from using the `/origin` command.

#### Why is SerializableData no longer a thing?
Apace did not know about codecs as a system when working on Origins, which lead to a redundant system.
We all want to work with Minecraft for the rewrite, rather than against it, so this is one of the steps we are taking.