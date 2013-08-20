package com.getbase.android.db.example.ui;

import com.getbase.android.db.example.model.Person;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.SortedMap;

public class SectionedPeopleList {

  private List<Person> people;
  private SortedMap<Integer, Character> sections;

  public SectionedPeopleList(List<Person> people) {
    this.people = ImmutableList.copyOf(people);
    naiveAlgorithmInitialize();
  }

  private void naiveAlgorithmInitialize() {
    Character currSection = null;
    sections = Maps.newTreeMap();
    int index = 0;
    for (Person person : people) {
      final Character section = person.getSection();
      if (!section.equals(currSection)) {
        sections.put(sections.size() + index, section);
        currSection = section;
      }
      index++;
    }
  }

  public boolean isSection(int position) {
    return sections.get(position) != null;
  }

  public char getSection(int position) {
    return sections.get(position);
  }

  public Person getItem(int position) {
    int positionInList = position - sections.headMap(position).size();
    return people.get(positionInList);
  }

  public int getCount() {
    return people.size() + sections.size();
  }
}
