package com.softwaremill.codebrag.service.user

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.UserDAO
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.followups.WelcomeFollowupsGenerator

class NewUserAdderTest
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var welcomeFollowupGenerator: WelcomeFollowupsGenerator = _
  var userDao: UserDAO = _
  var eventBus: EventBus = _
  var reviewTaskGenerator: CommitReviewTaskGenerator = _
  
  var userAdder: NewUserAdder = _

  override def beforeEach() {
    welcomeFollowupGenerator = mock[WelcomeFollowupsGenerator]
    userDao = mock[UserDAO]
    eventBus = mock[EventBus]
    reviewTaskGenerator = mock[CommitReviewTaskGenerator]    
    userAdder = new NewUserAdder(userDao, eventBus, reviewTaskGenerator, welcomeFollowupGenerator)
  }

  it should "build new user event using registered user's data" in {
    // Given
    val user = UserAssembler.randomUser.get
    when(userDao.add(user)).thenReturn(user)

    // When
    userAdder.add(user)

    // Then
    val expectedNewUserEvent = NewUserRegistered(user)
    verify(eventBus).publish(expectedNewUserEvent)
  }
  
  it should "generate review tasks and welcome followups for user" in {
    // Given
    val user = UserAssembler.randomUser.get
    when(userDao.add(user)).thenReturn(user)

    // When
    userAdder.add(user)

    // Then
    val newUserRegistered = NewUserRegistered(user)
    verify(reviewTaskGenerator).handleNewUserRegistered(newUserRegistered)
    verify(welcomeFollowupGenerator).createWelcomeFollowupFor(newUserRegistered)
  }

}
